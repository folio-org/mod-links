package org.folio.entlinks.service.messaging.authority.handler;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.config.properties.InstanceAuthorityChangeProperties;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.repository.InstanceLinkRepository;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingService;
import org.folio.qm.domain.dto.InventoryEvent;
import org.folio.qm.domain.dto.InventoryEventType;
import org.folio.qm.domain.dto.LinksEvent;
import org.folio.qm.domain.dto.LinksEventUpdateTargets;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteAuthorityChangeHandler implements AuthorityChangeHandler {

  private final InstanceLinkRepository linkRepository;
  private final InstanceAuthorityLinkingService linkService;
  private final InstanceAuthorityChangeProperties instanceAuthorityChangeProperties;

  @Override
  public List<LinksEvent> handle(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }

    var linksEvents = getLinksMap(events).entrySet()
      .stream()
      .map(entry -> Lists.partition(entry.getValue(), instanceAuthorityChangeProperties.getNumPartitions())
        .stream()
        .map(partition -> constructBaseEvent(entry.getKey(), partition))
        .toList())
      .flatMap(Collection::stream)
      .toList();

    linkService.deleteByAuthorityIdIn(events.stream().map(InventoryEvent::getId).toList());
    return linksEvents;
  }

  @Override
  public InventoryEventType supportedInventoryEventType() {
    return InventoryEventType.DELETE;
  }

  private Map<UUID, List<InstanceAuthorityLink>> getLinksMap(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyMap();
    }
    var authorityIds = events.stream().map(InventoryEvent::getId).toList();
    var links = linkRepository.findByAuthorityIdIn(authorityIds);
    return links.stream().collect(Collectors.groupingBy(InstanceAuthorityLink::getAuthorityId));
  }

  private List<LinksEventUpdateTargets> toEventMarcBibs(List<InstanceAuthorityLink> partition) {
    return partition.stream()
      .collect(Collectors.groupingBy(InstanceAuthorityLink::getBibRecordTag))
      .entrySet().stream()
      .map(e -> new LinksEventUpdateTargets().field(e.getKey())
        .instanceIds(e.getValue().stream().map(InstanceAuthorityLink::getInstanceId).toList()))
      .toList();
  }

  private LinksEvent constructBaseEvent(UUID authorityId, List<InstanceAuthorityLink> partition) {
    return new LinksEvent().jobId(UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e"))
      .type(LinksEvent.TypeEnum.DELETE)
      .authorityId(authorityId)
      .updateTargets(toEventMarcBibs(partition))
      .ts(String.valueOf(System.currentTimeMillis()));
  }
}
