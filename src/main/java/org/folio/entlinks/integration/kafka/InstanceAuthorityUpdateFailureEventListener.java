package org.folio.entlinks.integration.kafka;

import static org.folio.spring.tools.config.RetryTemplateConfiguration.DEFAULT_KAFKA_RETRY_TEMPLATE_NAME;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.folio.entlinks.domain.dto.LinkUpdateReport;
import org.folio.entlinks.service.links.AuthorityDataStatService;
import org.folio.spring.tools.batch.MessageBatchProcessor;
import org.folio.spring.tools.systemuser.SystemUserScopedExecutionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class InstanceAuthorityUpdateFailureEventListener {

  private final SystemUserScopedExecutionService executionService;
  private final MessageBatchProcessor messageBatchProcessor;
  private final AuthorityDataStatService dataStatService;

  @KafkaListener(id = "mod-entities-links-instance-authority-update-failure-listener",
    containerFactory = "statsListenerFactory",
    topicPattern = "#{folioKafkaProperties.listener['instance-authority-update-failure'].topicPattern}",
    groupId = "#{folioKafkaProperties.listener['instance-authority-update-failure'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['instance-authority-update-failure'].concurrency}")
  public void handleEvents(ConsumerRecord<String, LinkUpdateReport> consumerRecord) {
    LinkUpdateReport linkUpdateReport = consumerRecord.value();
    log.debug("Processing authority update failed from Kafka event {}", linkUpdateReport);
    String tenantId = linkUpdateReport.getTenant();
    executionService.executeSystemUserScoped(tenantId, () -> {
      log.info("Triggering update failures for stats records [tenant: {}]", tenantId);
      messageBatchProcessor.consumeBatchWithFallback(List.of(linkUpdateReport), DEFAULT_KAFKA_RETRY_TEMPLATE_NAME,
        this::handleReportEventsByJobId, this::logFailedEvent);
      return null;
    });
  }

  private void handleReportEventsByJobId(List<LinkUpdateReport> events) {
    events.stream()
      .collect(Collectors.groupingBy(LinkUpdateReport::getJobId))
      .forEach(dataStatService::updateForReports);
  }

  private void logFailedEvent(LinkUpdateReport event, Exception e) {
    if (event == null) {
      log.warn("Failed to process stats event [event: null]", e);
      return;
    }

    log.warn(() -> new FormattedMessageFactory().newMessage(
      "Failed to process stats event [jobId: {}, instanceId: {}, tenant: {}]", event.getJobId(), event.getInstanceId(),
      event.getTenant()), e);
  }
}
