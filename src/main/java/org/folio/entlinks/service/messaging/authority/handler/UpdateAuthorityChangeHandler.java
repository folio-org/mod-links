package org.folio.entlinks.service.messaging.authority.handler;

import static java.util.Collections.singletonList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.entlinks.config.properties.AuthorityChangeProperties;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.folio.entlinks.integration.internal.AuthoritySourceFilesService;
import org.folio.entlinks.integration.internal.AuthoritySourceRecordService;
import org.folio.entlinks.repository.InstanceLinkRepository;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingRulesService;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingService;
import org.folio.entlinks.service.messaging.authority.AuthorityMappingRulesProcessingService;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChange;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChangeHolder;
import org.folio.entlinks.service.messaging.authority.model.SubfieldsHolder;
import org.folio.qm.domain.dto.InventoryEvent;
import org.folio.qm.domain.dto.LinksEvent;
import org.folio.qm.domain.dto.LinksEventSubfields;
import org.folio.qm.domain.dto.LinksEventSubfieldsChanges;
import org.folio.qm.domain.dto.LinksEventUpdateTargets;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class UpdateAuthorityChangeHandler implements AuthorityChangeHandler {

  private final InstanceLinkRepository linkRepository;
  private final AuthorityChangeProperties authorityChangeProperties;
  private final AuthoritySourceFilesService sourceFilesService;
  private final AuthorityMappingRulesProcessingService authorityMappingRulesProcessingService;
  private final AuthoritySourceRecordService authoritySourceRecordService;
  private final InstanceAuthorityLinkingRulesService instanceAuthorityLinkingRulesService;
  private final InstanceAuthorityLinkingService linkService;

  @Override
  public List<LinksEvent> handle(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }

    List<LinksEvent> linksEvents = new ArrayList<>();

    var authorityChanges = events.stream()
      .map(event -> {
        var difference = getDifference(event.getNew(), event.getOld());
        if (difference.size() > 2 || difference.size() == 2 && difference.contains(AuthorityChange.NATURAL_ID)) {
          throw new IllegalArgumentException("Unsupported authority change");
        }
        return new AuthorityChangeHolder(event, difference);
      })
      .toList();

    var authorityChangesByType =
      authorityChanges.stream().collect(Collectors.partitioningBy(AuthorityChangeHolder::isOnlyNaturalIdChanged));

    authorityChangesByType.get(true).stream()
      .map(this::handleNaturalIdChange)
      .forEach(linksEvents::addAll);

    authorityChangesByType.get(false).stream()
      .map(this::handleFieldChange)
      .forEach(linksEvents::addAll);

    return linksEvents;
  }

  private List<LinksEvent> handleFieldChange(AuthorityChangeHolder changeHolder) {
    List<LinksEvent> linksEvents = new ArrayList<>();

    var authorityId = changeHolder.getAuthorityId();
    var naturalId = changeHolder.getNewNaturalId();
    var naturalIdChanged = changeHolder.isNaturalIdChanged();

    var subfield0Change = getSubfield0Change(changeHolder, naturalId, naturalIdChanged);

    var authoritySourceRecord = authoritySourceRecordService.getAuthoritySourceRecordById(authorityId);
    var changedTag = authorityMappingRulesProcessingService.getTagByAuthorityChange(changeHolder.getFieldChange());
    var linkingRuleForField = instanceAuthorityLinkingRulesService.getLinkingRulesByAuthorityField(changedTag);

    var dataField = authoritySourceRecord.content().getDataFields().stream()
      .filter(field -> field.getTag().equals(changedTag))
      .findFirst()
      .orElseThrow(() -> new FolioIntegrationException("Source record don't contains [tag: " + changedTag + "]"));

    var fieldsByBibTag = linkingRuleForField.stream()
      .collect(Collectors.toMap(InstanceAuthorityLinkingRule::getBibField,
        linkingRuleDto -> new SubfieldsHolder(dataField, linkingRuleDto)));

    fieldsByBibTag.forEach((tag, subfields)
      -> linkService.updateSubfieldsAndNaturalId(subfields.getBibSubfieldCodes(), naturalId, authorityId, tag));

    var linksEventSubfieldsChanges = fieldsByBibTag.entrySet().stream()
      .map(e -> {
        var subfieldsChange = e.getValue().toSubfieldsChange();
        subfield0Change.ifPresent(subfieldsChange::add);
        return new LinksEventSubfieldsChanges().field(e.getKey()).subfields(subfieldsChange);
      })
      .toList();

    Pageable pageable = PageRequest.of(0, authorityChangeProperties.getPartitionSize());
    do {
      var linksPage = linkRepository.findByAuthorityId(authorityId, pageable);
      var instanceLinks = linksPage.getContent();

      var linksEvent = constructBaseEvent(authorityId, instanceLinks, linksEventSubfieldsChanges);
      linksEvents.add(linksEvent);

      pageable = linksPage.nextPageable();
    } while (pageable.isPaged());

    return linksEvents;
  }

  @NotNull
  private Optional<LinksEventSubfields> getSubfield0Change(AuthorityChangeHolder changeHolder, String naturalId,
                                                           boolean naturalIdChanged) {
    Optional<LinksEventSubfields> subfield0Change = Optional.empty();
    if (naturalIdChanged) {
      subfield0Change = Optional.of(getSubfield0Value(naturalId, changeHolder.getNewSourceFileId()));
    }
    return subfield0Change;
  }

  private List<LinksEvent> handleNaturalIdChange(AuthorityChangeHolder changeHolder) {
    List<LinksEvent> linksEvents = new ArrayList<>();

    var authorityId = changeHolder.getAuthorityId();
    var naturalId = changeHolder.getNewNaturalId();
    linkService.updateNaturalId(naturalId, authorityId);

    var subfield0Change = getSubfield0Value(naturalId, changeHolder.getNewSourceFileId());

    Pageable pageable = PageRequest.of(0, authorityChangeProperties.getPartitionSize());
    do {
      var linksPage = linkRepository.findByAuthorityId(authorityId, pageable);
      var instanceLinks = linksPage.getContent();

      var subfieldsChanges = instanceLinks.stream()
        .map(InstanceAuthorityLink::getBibRecordTag)
        .distinct()
        .map(tag -> new LinksEventSubfieldsChanges().field(tag).subfields(singletonList(subfield0Change)))
        .toList();

      var linksEvent = constructBaseEvent(authorityId, instanceLinks, subfieldsChanges);
      linksEvents.add(linksEvent);

      pageable = linksPage.nextPageable();
    } while (pageable.isPaged());

    return linksEvents;
  }

  private LinksEventSubfields getSubfield0Value(String naturalId, UUID sourceFileId) {
    String subfield0Value = "";
    if (sourceFileId != null) {
      var baseUrl = sourceFilesService.fetchAuthoritySourceUrls().get(sourceFileId);
      subfield0Value = StringUtils.appendIfMissing(baseUrl, "/");
    }
    return new LinksEventSubfields().code("0").value(subfield0Value + naturalId);
  }

  private List<LinksEventUpdateTargets> toEventMarcBibs(List<InstanceAuthorityLink> partition) {
    return partition.stream()
      .collect(Collectors.groupingBy(InstanceAuthorityLink::getBibRecordTag))
      .entrySet().stream()
      .map(e -> new LinksEventUpdateTargets().field(e.getKey())
        .instanceIds(e.getValue().stream().map(InstanceAuthorityLink::getInstanceId).toList()))
      .toList();
  }

  private LinksEvent constructBaseEvent(UUID authorityId, List<InstanceAuthorityLink> partition,
                                        List<LinksEventSubfieldsChanges> subfieldChanges) {
    return new LinksEvent().jobId(UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e"))
      .type(LinksEvent.TypeEnum.UPDATE)
      .authorityId(authorityId)
      .updateTargets(toEventMarcBibs(partition))
      .subfieldsChanges(subfieldChanges)
      .ts(String.valueOf(System.currentTimeMillis()));
  }

  @SneakyThrows
  private List<AuthorityChange> getDifference(Object s1, Object s2) {
    List<AuthorityChange> values = new ArrayList<>();
    for (Method method : s1.getClass().getMethods()) {
      if (method.getName().startsWith("get")) {
        var value1 = method.invoke(s1);
        var value2 = method.invoke(s2);
        if (!Objects.equals(value1, value2)) {
          var fieldName = method.getName().substring(3);
          try {
            values.add(AuthorityChange.fromValue(fieldName));
          } catch (IllegalArgumentException e) {
            log.debug("Not supported authority change [fieldName: {}]", fieldName);
          }
        }
      }
    }
    return values;
  }

}
