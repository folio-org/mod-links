package org.folio.entlinks.service.authority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.entlinks.domain.dto.AuthoritySourceFileDto;
import org.folio.entlinks.integration.dto.event.DomainEvent;
import org.folio.entlinks.integration.dto.event.DomainEventType;
import org.folio.entlinks.integration.kafka.EventProducer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthoritySourceFileDomainEventPublisherTest {
  private static final String TENANT_ID = "test";
  private static final String DOMAIN_EVENT_TYPE_HEADER = "domain-event-type";

  @Mock
  private EventProducer<DomainEvent<?>> eventProducer;

  @Mock
  private FolioExecutionContext folioExecutionContext;

  @InjectMocks
  private AuthoritySourceFileDomainEventPublisher eventPublisher;

  private final ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);

  @Test
  void shouldNotSendUpdatedEventWhenIdIsNull() {
    // when
    eventPublisher.publishUpdateEvent(new AuthoritySourceFileDto(), new AuthoritySourceFileDto());

    // then
    verifyNoInteractions(eventProducer);
  }

  @Test
  void shouldSendUpdatedEvent() {
    // given
    var id = UUID.randomUUID();
    var oldDto = new AuthoritySourceFileDto().id(id).source(AuthoritySourceFileDto.SourceEnum.FOLIO);
    var newDto = new AuthoritySourceFileDto().id(id).source(AuthoritySourceFileDto.SourceEnum.FOLIO);
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT_ID);

    // when
    eventPublisher.publishUpdateEvent(oldDto, newDto);

    // then
    verify(eventProducer).sendMessage(eq(oldDto.getId().toString()), captor.capture(), eq(DOMAIN_EVENT_TYPE_HEADER),
      eq(DomainEventType.UPDATE));
    assertEquals(newDto, captor.getValue().getNewEntity());
    assertEquals(oldDto, captor.getValue().getOldEntity());
    assertEquals(TENANT_ID, captor.getValue().getTenant());
  }
}
