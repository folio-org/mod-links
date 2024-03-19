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

  @Qualifier("authoritySourceFile")
  private final EventProducer<DomainEvent<?>> eventProducer;
  private final FolioExecutionContext folioExecutionContext;

  public void publishUpdateEvent(AuthoritySourceFileDto oldAuthSrcFile, AuthoritySourceFileDto updatedAuthSrcFile) {
    var id = updatedAuthSrcFile.getId();
    if (id == null || oldAuthSrcFile.getId() == null) {
      log.warn("Old/New Authority Source File cannot have null id: updated.id - {}, old.id: {}",
        id, oldAuthSrcFile.getId());
      return;
    }

    log.debug("publishUpdated::process authority source file id={}", id);

    var domainEvent = DomainEvent.updateEvent(id, oldAuthSrcFile, updatedAuthSrcFile,
      folioExecutionContext.getTenantId());
    eventProducer.sendMessage(id.toString(), domainEvent, DOMAIN_EVENT_TYPE_HEADER, DomainEventType.UPDATE);
  }
}
