package org.folio.entlinks.service.links;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.folio.entlinks.utils.DateUtils.toTimestamp;
import static org.folio.entlinks.utils.FieldUtils.getSubfield0Value;
import static org.folio.entlinks.utils.LinkEventsUtils.constructEvent;

import jakarta.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.SearchClient;
import org.folio.entlinks.client.SourceStorageClient;
import org.folio.entlinks.domain.dto.Authority;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.LinkStatus;
import org.folio.entlinks.domain.dto.LinksChangeEvent;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.dto.SubfieldChange;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkStatus;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.domain.entity.projection.LinkCountView;
import org.folio.entlinks.domain.repository.InstanceLinkRepository;
import org.folio.entlinks.integration.internal.AuthoritySourceFilesService;
import org.folio.entlinks.integration.kafka.EventProducer;
import org.folio.entlinks.service.messaging.authority.model.FieldChangeHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class InstanceAuthorityLinkingService {

  private static final String SEEK_FIELD = "updatedAt";

  private final InstanceLinkRepository instanceLinkRepository;
  private final AuthorityDataService authorityDataService;
  private final SearchClient searchClient;
  private final SourceStorageClient sourceStorageClient;
  private final InstanceAuthorityLinkingRulesService linkingRulesService;
  private final AuthoritySourceFilesService sourceFilesService;
  private final EventProducer<LinksChangeEvent> eventProducer;

  public List<InstanceAuthorityLink> getLinksByInstanceId(UUID instanceId) {
    log.info("Loading links for [instanceId: {}]", instanceId);
    return instanceLinkRepository.findByInstanceId(instanceId);
  }

  public Page<InstanceAuthorityLink> getLinksByAuthorityId(UUID authorityId, Pageable pageable) {
    log.info("Loading links for [authorityId: {}, page size: {}, page num: {}]", authorityId,
      pageable.getPageSize(), pageable.getOffset());
    return instanceLinkRepository.findByAuthorityId(authorityId, pageable);
  }

  public List<InstanceAuthorityLink> getLinksByIds(List<Integer> ids) {
    log.info("Retrieving links by ids [{}]", ids);
    var longIds = ids.stream()
      .mapToLong(Integer::longValue)
      .boxed()
      .toList();
    return instanceLinkRepository.findAllById(longIds);
  }

  //todo: REFACTOR
  @Transactional
  public void updateLinks(UUID instanceId, List<InstanceAuthorityLink> incomingLinks) {
    if (log.isDebugEnabled()) {
      log.debug("Update/renovate links for [instanceId: {}, links: {}]", instanceId, incomingLinks);
    } else {
      log.info("Update/renovate links for [instanceId: {}, links amount: {}]", instanceId, incomingLinks.size());
    }
    var linkingRules = linkingRulesService.getLinkingRules().stream()
      .collect(Collectors.toMap(InstanceAuthorityLinkingRule::getId, Function.identity()));
    incomingLinks.forEach(instanceAuthorityLink ->
      instanceAuthorityLink.setLinkingRule(linkingRules.get(instanceAuthorityLink.getLinkingRule().getId())));

    var existedLinks = instanceLinkRepository.findByInstanceId(instanceId);
    var invalidLinks = new LinkedList<InstanceAuthorityLink>();

    var authorityDataById = incomingLinks.stream()
      .map(InstanceAuthorityLink::getAuthorityData)
      .collect(Collectors.toMap(AuthorityData::getId, Function.identity(), (a1, a2) -> a1));
    var linksByAuthorityId = incomingLinks.stream()
      .collect(Collectors.groupingBy(link -> link.getAuthorityData().getId()));

    var authorityNaturalIdsByIds = fetchAuthorityNaturalIds(authorityDataById.keySet());
    var authoritySources = fetchAuthoritySources(linksByAuthorityId.keySet());

    var authorityDataSet = authorityDataById.values().stream()
      .map(authorityData -> {
        var authorityId = authorityData.getId();
        var naturalId = authorityNaturalIdsByIds.get(authorityId);
        var authority = authoritySources.stream()
          .filter(authorityRecord -> authorityRecord.getExternalIdsHolder().getAuthorityId().equals(authorityId))
          .findFirst();

        if (isNull(naturalId) || authority.isEmpty()) {
          invalidLinks.addAll(linksByAuthorityId.remove(authorityId));
          return null;
        }

        // todo: highlighted note: authority changed to invalid (heading change/subfield $t presence change) case
        var invalidLinksForAuthority = linksByAuthorityId.get(authorityId).stream()
          .filter(link -> !isLinkValid(authority.get().getParsedRecord().getContent().getFields(), link))
          .toList();
        if (!invalidLinksForAuthority.isEmpty()) {
          invalidLinks.addAll(invalidLinksForAuthority);
          linksByAuthorityId.get(authorityId).removeAll(invalidLinksForAuthority);
        }
        if (invalidLinksForAuthority.size() == linksByAuthorityId.get(authorityData.getId()).size()) {
          return null;
        }

        authorityData.setNaturalId(naturalId);
        return authorityData;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    var savedAuthorityData = authorityDataService.saveAll(authorityDataSet);
    var incomingValidLinks = linksByAuthorityId.values().stream().flatMap(Collection::stream).toList();
    var linksToDelete = subtract(existedLinks, incomingValidLinks);
    instanceLinkRepository.deleteAllInBatch(linksToDelete);


    for (InstanceAuthorityLink incomingLink : incomingValidLinks) {
      var linkAuthorityData = incomingLink.getAuthorityData();
      var authorityData = savedAuthorityData.get(linkAuthorityData.getId());
      incomingLink.setAuthorityData(authorityData);
      existedLinks.stream()
        .filter(existedLink -> existedLink.isSameLink(incomingLink))
        .findFirst()
        .ifPresent(existedLink -> incomingLink.setId(existedLink.getId()));
    }

    sendEvents(instanceId, renovateBibsForInvalidLinks(invalidLinks));
    sendEvents(instanceId, renovateBibsForValidLinks(instanceId, incomingValidLinks, authoritySources));
  }

  private List<LinksChangeEvent> renovateBibsForInvalidLinks(List<InstanceAuthorityLink> links) {
    var eventId = UUID.randomUUID();
    var linksByAuthorityId = links.stream()
      .collect(Collectors.groupingBy(link -> link.getAuthorityData().getId()));
    return linksByAuthorityId.entrySet().stream()
      .map(linksByAuthorityIdEntry -> constructEvent(eventId, linksByAuthorityIdEntry.getKey(),
        LinksChangeEvent.TypeEnum.DELETE, linksByAuthorityIdEntry.getValue(), emptyList()))
      .toList();
  } //todo: move to privates section

  private boolean isLinkValid(List<Map<String, FieldContent>> authorityFields, InstanceAuthorityLink link) {
    var authorityField = authorityFields.stream()
      .flatMap(fields -> fields.entrySet().stream())
      .filter(fieldContentEntry -> link.getLinkingRule().getAuthorityField().equals(fieldContentEntry.getKey()))
      .findFirst();

    return authorityField
      .filter(stringFieldContentEntry -> isSubfieldExist(stringFieldContentEntry.getValue(), link.getLinkingRule()))
      .isPresent();
  }

  private boolean isSubfieldExist(FieldContent authorityField, InstanceAuthorityLinkingRule linkingRule) {
    var existValidation = linkingRule.getSubfieldsExistenceValidations();
    if (isNotEmpty(existValidation)) {
      var authoritySubfields = authorityField.getSubfields();

      for (var subfieldExistence : existValidation.entrySet()) {
        var contains = authoritySubfields.stream()
          .anyMatch(subfieldMap -> subfieldMap.containsKey(subfieldExistence.getKey()));
        if (contains != subfieldExistence.getValue()) {
          return false;
        }
      }
    }
    return true;
  }

  private List<LinksChangeEvent> renovateBibsForValidLinks(UUID instanceId, List<InstanceAuthorityLink> links,
                                                           List<StrippedParsedRecord> authoritySources) {
    instanceLinkRepository.saveAll(links);

    var linksByAuthorityId = links.stream()
      .collect(Collectors.groupingBy(instanceAuthorityLink -> instanceAuthorityLink.getAuthorityData().getId()));
    var eventId = UUID.randomUUID();
    var events = new LinkedList<LinksChangeEvent>();

    linksByAuthorityId.entrySet().forEach(authorityIdToLinks -> {
      //todo: maybe move out to another method (other refactor also may be needed)
      var authority = authoritySources.stream()
        .filter(strippedParsedRecord -> authorityIdToLinks.getKey().equals(
          strippedParsedRecord.getExternalIdsHolder().getAuthorityId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Authority with id " + authorityIdToLinks.getKey()
            + " not found. Unable to renovate links for instanceId " + instanceId));
      var fieldChangeHolders = new LinkedList<FieldChangeHolder>();

      //todo: maybe to another method also
      authorityIdToLinks.getValue().forEach(instanceAuthorityLink -> {
        var changedTag = instanceAuthorityLink.getLinkingRule().getAuthorityField();
        authority.getParsedRecord().getContent().getFields().stream()
          .flatMap(fields -> fields.entrySet().stream())
          .filter(fieldEntry -> changedTag.equals(fieldEntry.getKey()))
          .findFirst()
          .map(Map.Entry::getValue)
          .ifPresent(authorityField -> {
            var fieldChangeHolder = new FieldChangeHolder(authorityField, instanceAuthorityLink.getLinkingRule());
            fieldChangeHolder.addExtraSubfieldChange(
              getSubfield0Change(instanceAuthorityLink.getAuthorityData().getNaturalId()));
            fieldChangeHolders.add(fieldChangeHolder);
          });
      });

      var fieldChanges = fieldChangeHolders.stream()
        .map(FieldChangeHolder::toFieldChange)
        .toList();

      events.add(constructEvent(eventId, authorityIdToLinks.getKey(), LinksChangeEvent.TypeEnum.UPDATE,
        authorityIdToLinks.getValue(), fieldChanges));
    });

    return events;
  } //todo: move to privates

  public Map<UUID, Integer> countLinksByAuthorityIds(Set<UUID> authorityIds) {
    if (log.isDebugEnabled()) {
      log.info("Count links for [authority ids: {}]", authorityIds);
    } else {
      log.info("Count links for [authority ids amount: {}]", authorityIds.size());
    }
    return instanceLinkRepository.countLinksByAuthorityIds(authorityIds).stream()
      .collect(Collectors.toMap(LinkCountView::getId, LinkCountView::getTotalLinks));
  }

  @Transactional
  public void updateStatus(UUID authorityId, InstanceAuthorityLinkStatus status, String errorCause) {
    log.info("Update links [authority id: {}, status: {}, errorCause: {}]", authorityId, status, errorCause);
    instanceLinkRepository.updateStatusAndErrorCauseByAuthorityId(status, trimToNull(errorCause), authorityId);
  }

  @Transactional
  public void deleteByAuthorityIdIn(Set<UUID> authorityIds) {
    if (log.isDebugEnabled()) {
      log.info("Delete links for [authority ids: {}]", authorityIds);
    } else {
      log.info("Delete links for [authority ids amount: {}]", authorityIds.size());
    }
    instanceLinkRepository.deleteByAuthorityIds(authorityIds);
    authorityDataService.markDeleted(authorityIds);
  }

  @Transactional
  public void saveAll(UUID instanceId, List<InstanceAuthorityLink> links) {
    log.info("Save links for [instanceId: {}, links amount: {}]", instanceId, links.size());
    log.debug("Save links for [instanceId: {}, links: {}]", instanceId, links);

    instanceLinkRepository.saveAll(links);
  }

  public List<InstanceAuthorityLink> getLinks(LinkStatus status, OffsetDateTime fromDate,
                                              OffsetDateTime toDate, int limit) {
    log.info("Fetching links for [status: {}, fromDate: {}, toDate: {}, limit: {}]",
      status, fromDate, toDate, limit);

    var linkStatus = status == null ? null : InstanceAuthorityLinkStatus.valueOf(status.getValue());
    var linkFromDate = fromDate == null ? null : toTimestamp(fromDate);
    var linkToDate = toDate == null ? null : toTimestamp(toDate);
    var pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc(SEEK_FIELD)));

    var specification = getSpecFromStatusAndDates(linkStatus, linkFromDate, linkToDate);
    return instanceLinkRepository.findAll(specification, pageable).getContent();
  }

  private List<InstanceAuthorityLink> subtract(Collection<InstanceAuthorityLink> source,
                                               Collection<InstanceAuthorityLink> target) {
    return new LinkedHashSet<>(source).stream()
      .filter(t -> target.stream().noneMatch(link -> link.isSameLink(t)))
      .toList();
  }

  private Specification<InstanceAuthorityLink> getSpecFromStatusAndDates(
    InstanceAuthorityLinkStatus status, Timestamp from, Timestamp to) {

    return (root, query, builder) -> {
      var predicates = new LinkedList<>();

      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (from != null) {
        predicates.add(builder.greaterThanOrEqualTo(root.get(SEEK_FIELD), from));
      }
      if (to != null) {
        predicates.add(builder.lessThanOrEqualTo(root.get(SEEK_FIELD), to));
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  private Map<UUID, String> fetchAuthorityNaturalIds(Set<UUID> authorityIds) {
    if (authorityIds.isEmpty()) {
      return emptyMap();
    }
    var searchQuery = searchClient.buildIdsQuery(authorityIds);
    return searchClient.searchAuthorities(searchQuery, false)
      .getAuthorities().stream()
      .collect(Collectors.toMap(Authority::getId, Authority::getNaturalId));
  }

  private List<StrippedParsedRecord> fetchAuthoritySources(Set<UUID> authorityIds) {
    if (authorityIds.isEmpty()) {
      return emptyList();
    }
    var authorityFetchRequest = sourceStorageClient.buildBatchFetchRequestForAuthority(authorityIds,
      linkingRulesService.getMinAuthorityField(),
      linkingRulesService.getMaxAuthorityField());
    return sourceStorageClient.fetchParsedRecordsInBatch(authorityFetchRequest).getRecords();
  }

  private SubfieldChange getSubfield0Change(String naturalId) {
    var sourceFiles = sourceFilesService.fetchAuthoritySources();
    var subfield0Value = getSubfield0Value(sourceFiles, naturalId);
    return new SubfieldChange().code("0").value(subfield0Value);
  }

  private void sendEvents(UUID instanceId, List<LinksChangeEvent> events) {
    if (!events.isEmpty()) {
      log.info("Sending {} events for instanceId {} to Kafka for links renovation process.", instanceId, events.size());
      eventProducer.sendMessages(events);
    }
  }
}
