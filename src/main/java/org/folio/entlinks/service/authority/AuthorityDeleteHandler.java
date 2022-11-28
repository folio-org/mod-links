package org.folio.entlinks.service.authority;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.config.properties.AuthorityChangeProperties;
import org.folio.entlinks.model.entity.InstanceLink;
import org.folio.entlinks.repository.InstanceLinkRepository;
import org.folio.entlinks.service.InstanceLinkService;
import org.folio.qm.domain.dto.InventoryEvent;
import org.folio.qm.domain.dto.LinksEvent;
import org.folio.qm.domain.dto.LinksEventUpdateTargets;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityDeleteHandler implements AuthorityChangeHandler {

  private final AuthorityChangeProperties authorityChangeProperties;
  private final InstanceLinkRepository linkRepository;
  private final InstanceLinkService linkService;

  @Override
  public List<LinksEvent> handle(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }

    var linksEvents = getLinksMap(events).entrySet()
      .stream()
      .map(entry -> Lists.partition(entry.getValue(), authorityChangeProperties.getPartitionSize())
        .stream()
        .map(partition -> constructBaseEvent(entry.getKey(), partition))
        .toList())
      .flatMap(Collection::stream)
      .toList();

    linkService.deleteByAuthorityIdIn(events.stream().map(InventoryEvent::getId).toList());
    return linksEvents;
  }

  private Map<UUID, List<InstanceLink>> getLinksMap(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyMap();
    }
    var authorityIds = events.stream().map(InventoryEvent::getId).toList();
    var links = linkRepository.findByAuthorityIdIn(authorityIds);
    return links.stream().collect(Collectors.groupingBy(InstanceLink::getAuthorityId));
  }

  private List<LinksEventUpdateTargets> toEventMarcBibs(List<InstanceLink> partition) {
    return partition.stream()
      .collect(Collectors.groupingBy(InstanceLink::getBibRecordTag))
      .entrySet().stream()
      .map(e -> new LinksEventUpdateTargets().field(e.getKey())
        .instanceIds(e.getValue().stream().map(InstanceLink::getInstanceId).toList()))
      .toList();
  }

  private LinksEvent constructBaseEvent(UUID authorityId, List<InstanceLink> partition) {
    return new LinksEvent().jobId(UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e"))
      .type(LinksEvent.TypeEnum.DELETE)
      .authorityId(authorityId)
      .updateTargets(toEventMarcBibs(partition))
      .ts(String.valueOf(System.currentTimeMillis()));
  }
}
