package org.folio.entlinks.service.messaging.authority.handler;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.entlinks.domain.dto.LinksChangeEvent.TypeEnum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.entlinks.config.properties.InstanceAuthorityChangeProperties;
import org.folio.entlinks.domain.dto.InventoryEvent;
import org.folio.entlinks.domain.dto.LinkUpdateReport;
import org.folio.entlinks.integration.dto.AuthoritySourceRecord;
import org.folio.entlinks.integration.internal.AuthoritySourceRecordService;
import org.folio.entlinks.service.links.AuthorityDataService;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingRulesService;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingService;
import org.folio.entlinks.service.messaging.authority.AuthorityMappingRulesProcessingService;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChange;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChangeField;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChangeHolder;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChangeType;
import org.folio.entlinks.utils.KafkaUtils;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.impl.RecordImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UpdateAuthorityChangeHandlerTest {
  private static final String TOPIC_NAME = "links.instance-authority-stats";

  private @Mock AuthorityMappingRulesProcessingService mappingRulesProcessingService;
  private @Mock AuthorityDataService authorityDataService;
  private @Mock AuthoritySourceRecordService sourceRecordService;
  private @Mock InstanceAuthorityLinkingRulesService linkingRulesService;
  private @Mock KafkaTemplate<String, LinkUpdateReport> linksUpdateKafkaTemplate;
  private @Mock FolioExecutionContext context;
  private @Mock InstanceAuthorityLinkingService linkingService;
  private @Mock InstanceAuthorityChangeProperties instanceAuthorityChangeProperties;
  private @InjectMocks UpdateAuthorityChangeHandler handler;

  @Test
  void getReplyEventType_positive() {
    var actual = handler.getReplyEventType();

    assertEquals(TypeEnum.UPDATE, actual);
  }

  @Test
  void supportedInventoryEventType_positive() {
    var actual = handler.supportedAuthorityChangeType();

    assertEquals(AuthorityChangeType.UPDATE, actual);
  }

  @Test
  void handle_positive() {
    UUID id = UUID.randomUUID();
    long currentTimeMillis = System.currentTimeMillis();
    var changes = Map.of(
      AuthorityChangeField.PERSONAL_NAME, new AuthorityChange(AuthorityChangeField.PERSONAL_NAME, "new", "old")
    );
    var event = new AuthorityChangeHolder(new InventoryEvent().id(id), changes, emptyMap(), 0);
    var report = new LinkUpdateReport();

    report.setFailCause("Source record don't contains [authorityId: " + id + ", tag: notExistingTag]");
    report.setInstanceId(event.getAuthorityId());
    report.setTenant(context.getTenantId());
    report.setStatus(LinkUpdateReport.StatusEnum.FAIL);
    report.setTs(String.valueOf(currentTimeMillis).substring(0, 10));

    when(mappingRulesProcessingService.getTagByAuthorityChangeField(any())).thenReturn("notExistingTag");
    when(sourceRecordService.getAuthoritySourceRecordById(any()))
      .thenReturn(new AuthoritySourceRecord(id, UUID.randomUUID(), new RecordImpl()));

    var topicName = KafkaUtils.getTenantTopicName(TOPIC_NAME, context.getTenantId());
    var producerRecord = new ProducerRecord<String, LinkUpdateReport>(topicName, report);

    handler.handle(List.of(event));

    verify(linksUpdateKafkaTemplate).send(producerRecord);
    verify(mappingRulesProcessingService).getTagByAuthorityChangeField(AuthorityChangeField.PERSONAL_NAME);
    verify(sourceRecordService).getAuthoritySourceRecordById(id);
  }

  @Test
  void handle_positive_emptyEventList() {
    var actual = handler.handle(emptyList());

    assertThat(actual).isEmpty();
  }

  @Test
  void handle_positive_nullEventList() {
    var actual = handler.handle(null);

    assertThat(actual).isEmpty();
  }
}
