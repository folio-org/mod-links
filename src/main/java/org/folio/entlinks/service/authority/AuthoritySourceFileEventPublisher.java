package org.folio.entlinks.service.authority;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.dto.AuthoritySourceFileDto;
import org.folio.entlinks.integration.dto.event.DomainEvent;
import org.folio.entlinks.integration.dto.event.DomainEventType;
import org.folio.entlinks.integration.kafka.EventProducer;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthoritySourceFileEventPublisher {
  private static final String DOMAIN_EVENT_TYPE_HEADER = "domain-event-type";

  @Qualifier("authoritySourceFileMessageProducer")
  private final EventProducer<DomainEvent<?>> eventProducer;
  private final FolioExecutionContext folioExecutionContext;

  public void publishUpdateEvent(AuthoritySourceFileDto oldAsfDto, AuthoritySourceFileDto updatedAsfDto) {
    var id = updatedAsfDto.getId();
    if (id == null || oldAsfDto.getId() == null) {
      log.warn("Old/New Authority Source File cannot have null id: updated.id - {}, old.id: {}",
        id, oldAsfDto.getId());
      return;
    }

    log.debug("publishUpdated::process authority source file id={}", id);

    var domainEvent = DomainEvent.updateEvent(id, oldAsfDto, updatedAsfDto,
      folioExecutionContext.getTenantId());
    eventProducer.sendMessage(id.toString(), domainEvent, DOMAIN_EVENT_TYPE_HEADER, DomainEventType.UPDATE);
  }
}
