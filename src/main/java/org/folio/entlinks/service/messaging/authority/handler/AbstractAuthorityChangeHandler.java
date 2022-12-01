package org.folio.entlinks.service.messaging.authority.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.config.properties.InstanceAuthorityChangeProperties;
import org.folio.entlinks.domain.dto.ChangeTarget;
import org.folio.entlinks.domain.dto.FieldChange;
import org.folio.entlinks.domain.dto.LinksChangeEvent;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public abstract class AbstractAuthorityChangeHandler implements AuthorityChangeHandler {

  private final InstanceAuthorityChangeProperties instanceAuthorityChangeProperties;
  private final InstanceAuthorityLinkingService linkService;

  protected List<LinksChangeEvent> handleLinksByPartitions(UUID authorityId,
                                                           Function<List<InstanceAuthorityLink>,
                                                             LinksChangeEvent> function) {
    List<LinksChangeEvent> linksEvents = new ArrayList<>();
    Pageable pageable = PageRequest.of(0, instanceAuthorityChangeProperties.getNumPartitions());
    do {
      var linksPage = linkService.findByAuthorityId(authorityId, pageable);
      var instanceLinks = linksPage.getContent();

      linksEvents.add(function.apply(instanceLinks));

      pageable = linksPage.nextPageable();
    } while (pageable.isPaged());
    return linksEvents;
  }

  protected LinksChangeEvent constructEvent(UUID jobId, UUID authorityId,
                                            List<InstanceAuthorityLink> partition,
                                            List<FieldChange> fieldChanges) {
    return new LinksChangeEvent()
      .jobId(jobId)
      .type(getReplyEventType())
      .authorityId(authorityId)
      .updateTargets(toChangeTargets(partition))
      .subfieldsChanges(fieldChanges)
      .ts(String.valueOf(System.currentTimeMillis()));
  }

  private List<ChangeTarget> toChangeTargets(List<InstanceAuthorityLink> partition) {
    return partition.stream()
      .collect(Collectors.groupingBy(InstanceAuthorityLink::getBibRecordTag))
      .entrySet().stream()
      .map(e -> new ChangeTarget().field(e.getKey())
        .instanceIds(e.getValue().stream().map(InstanceAuthorityLink::getInstanceId).toList()))
      .toList();
  }
}
