package org.folio.entlinks.service.messaging.authority.handler;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.folio.entlinks.config.properties.InstanceAuthorityChangeProperties;
import org.folio.entlinks.domain.dto.InventoryEvent;
import org.folio.entlinks.domain.dto.InventoryEventType;
import org.folio.entlinks.domain.dto.LinksChangeEvent;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingService;
import org.springframework.stereotype.Component;

@Component
public class DeleteAuthorityChangeHandler extends AbstractAuthorityChangeHandler {

  private final InstanceAuthorityLinkingService linkService;

  public DeleteAuthorityChangeHandler(InstanceAuthorityLinkingService linkService,
                                      InstanceAuthorityChangeProperties instanceAuthorityChangeProperties) {
    super(instanceAuthorityChangeProperties, linkService);
    this.linkService = linkService;
  }

  @Override
  public List<LinksChangeEvent> handle(List<InventoryEvent> events) {
    if (events == null || events.isEmpty()) {
      return emptyList();
    }

    List<LinksChangeEvent> linksEvents = new ArrayList<>();

    for (InventoryEvent event : events) {
      var linksChangeEvents =
        handleLinksByPartitions(event.getId(),
          instanceLinks -> constructEvent(UUID.randomUUID(), event.getId(), instanceLinks, emptyList())
        );
      linksEvents.addAll(linksChangeEvents);
    }


    linkService.deleteByAuthorityIdIn(events.stream().map(InventoryEvent::getId).collect(Collectors.toSet()));
    return linksEvents;
  }

  @Override
  public LinksChangeEvent.TypeEnum getReplyEventType() {
    return LinksChangeEvent.TypeEnum.DELETE;
  }

  @Override
  public InventoryEventType supportedInventoryEventType() {
    return InventoryEventType.DELETE;
  }

}
