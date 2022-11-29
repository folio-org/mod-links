package org.folio.entlinks.service.messaging.authority;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.entlinks.service.messaging.authority.handler.DeleteAuthorityChangeHandler;
import org.folio.entlinks.service.messaging.authority.handler.UpdateAuthorityChangeHandler;
import org.folio.entlinks.utils.KafkaUtils;
import org.folio.qm.domain.dto.InventoryEvent;
import org.folio.qm.domain.dto.InventoryEventType;
import org.folio.qm.domain.dto.LinksEvent;
import org.folio.spring.FolioExecutionContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorityInstanceLinkUpdateService {

  private final FolioExecutionContext context;
  private final KafkaTemplate<String, LinksEvent> kafkaTemplate;
  private final UpdateAuthorityChangeHandler authorityUpdateHandlingService;
  private final DeleteAuthorityChangeHandler deleteAuthorityChangeHandler;

  public void handleAuthoritiesChanges(List<InventoryEvent> events) {
    var eventsByType = events.stream().collect(Collectors.groupingBy(InventoryEvent::getType));
    var deleteEvents = deleteAuthorityChangeHandler.handle(eventsByType.get(InventoryEventType.DELETE.getValue()));
    sendEvents(deleteEvents);
    var updateEvents = authorityUpdateHandlingService.handle(eventsByType.get(InventoryEventType.UPDATE.getValue()));
    sendEvents(updateEvents);
  }

  private void sendEvents(List<LinksEvent> events) {
    log.info("Sending {} events to Kafka", events.size());
    events.stream()
      .map(this::toProducerRecord)
      .forEach(kafkaTemplate::send);
  }

  private ProducerRecord<String, LinksEvent> toProducerRecord(LinksEvent linksEvent) {
    linksEvent.tenant(context.getTenantId());
    var producerRecord = new ProducerRecord<String, LinksEvent>(topicName(), linksEvent);
    KafkaUtils.toKafkaHeaders(context.getOkapiHeaders())
      .forEach(header -> producerRecord.headers().add(header));
    return producerRecord;
  }

  private String topicName() {
    return KafkaUtils.getTenantTopicName("links.instance-authority", context.getTenantId());
  }

}
