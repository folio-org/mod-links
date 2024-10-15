package org.folio.entlinks.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.service.tenant.MarcSpecificationUpdateService;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SpecificationChangedEventListener {

  private final SystemUserScopedExecutionService executionService;
  private final MarcSpecificationUpdateService updateService;

  @KafkaListener(id = "mod-entities-links-specification-storage-listener",
                 containerFactory = "specificationListenerFactory",
                 topicPattern = "#{folioKafkaProperties.listener['specification-storage'].topicPattern}",
                 groupId = "#{folioKafkaProperties.listener['specification-storage'].groupId}",
                 concurrency = "#{folioKafkaProperties.listener['specification-storage'].concurrency}")
  public void handleEvent(SpecificationUpdatedEvent event) {
    log.info("Processing specification changed Kafka event [{}]", event);
    if (event.updateExtent() == SpecificationUpdatedEvent.UpdateExtent.FULL) {
      executionService.executeSystemUserScoped(event.tenantId(), () -> {
        updateService.sendSpecificationRequests();
        return null;
      });
    }
  }

}
