package org.folio.entlinks.integration.kafka;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.entlinks.domain.dto.AuthorityDataStatActionDto.UPDATE_HEADING;
import static org.folio.entlinks.domain.dto.LinkUpdateReport.StatusEnum.FAIL;
import static org.folio.support.KafkaTestUtils.createAndStartTestConsumer;
import static org.folio.support.TestDataUtils.Link.TAGS;
import static org.folio.support.TestDataUtils.linksDto;
import static org.folio.support.TestDataUtils.linksDtoCollection;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.folio.support.base.TestConstants.USER_ID;
import static org.folio.support.base.TestConstants.authorityStatsEndpoint;
import static org.folio.support.base.TestConstants.inventoryAuthorityTopic;
import static org.folio.support.base.TestConstants.linksInstanceAuthorityStatsTopic;
import static org.folio.support.base.TestConstants.linksInstanceAuthorityTopic;
import static org.folio.support.base.TestConstants.linksInstanceEndpoint;
import static org.folio.support.base.TestConstants.linksStatsInstanceEndpoint;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ThreadUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.dto.AuthorityInventoryRecord;
import org.folio.entlinks.domain.dto.AuthorityInventoryRecordMetadata;
import org.folio.entlinks.domain.dto.LinkStatus;
import org.folio.entlinks.domain.dto.LinkUpdateReport;
import org.folio.entlinks.domain.dto.LinksChangeEvent;
import org.folio.spring.test.extension.DatabaseCleanup;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.support.DatabaseHelper;
import org.folio.support.TestDataUtils;
import org.folio.support.TestDataUtils.Link;
import org.folio.support.base.IntegrationTestBase;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

@IntegrationTest
@DatabaseCleanup(tables = {DatabaseHelper.INSTANCE_AUTHORITY_LINK_TABLE,
                           DatabaseHelper.AUTHORITY_DATA_STAT_TABLE,
                           DatabaseHelper.AUTHORITY_DATA_TABLE})
class InstanceAuthorityStatsEventListenerIT extends IntegrationTestBase {
  private static final OffsetDateTime TO_DATE = OffsetDateTime.of(LocalDateTime.now().plusHours(1), ZoneOffset.UTC);
  private static final OffsetDateTime FROM_DATE = TO_DATE.minusMonths(1);
  private KafkaMessageListenerContainer<String, LinksChangeEvent> container;
  private BlockingQueue<ConsumerRecord<String, LinksChangeEvent>> consumerRecords;

  @BeforeEach
  void setUp(@Autowired KafkaProperties kafkaProperties) {
    consumerRecords = new LinkedBlockingQueue<>();
    container = createAndStartTestConsumer(linksInstanceAuthorityTopic(), consumerRecords, kafkaProperties,
      LinksChangeEvent.class);
  }

  @AfterEach
  void tearDown() {
    container.stop();
  }

  @Test
  @SneakyThrows
  void shouldHandleEvent_positive() {
    var instanceId = UUID.randomUUID();
    var authorityId = UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e");
    var link = new Link(authorityId, TAGS[0]);

    prepareData(instanceId, authorityId, link);

    var linksChangeEvent = Objects.requireNonNull(getReceivedEvent()).value();

    // prepare and send instance authority stats event
    var linkId = linksChangeEvent.getUpdateTargets().get(0).getLinks().get(0).getLinkId();
    var failCause = "test";
    var event = new LinkUpdateReport()
      .tenant(TENANT_ID)
      .jobId(linksChangeEvent.getJobId())
      .instanceId(instanceId)
      .status(FAIL)
      .linkIds(singletonList(linkId.intValue()))
      .failCause(failCause);
    sendKafkaMessage(linksInstanceAuthorityStatsTopic(), event.getJobId().toString(), event);

    assertLinksUpdated(failCause);
  }

  @Test
  @SneakyThrows
  void shouldHandleEvent_positive_whenLinkIdsAndInstanceIdAreEmpty() {
    var instanceId = UUID.randomUUID();
    var authorityId = UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e");
    var link = new Link(authorityId, TAGS[0]);

    // save link
    prepareData(instanceId, authorityId, link);

    var linksChangeEvent = Objects.requireNonNull(getReceivedEvent()).value();

    // prepare and send instance authority stats event
    var failCause = "test";
    var event = new LinkUpdateReport()
      .tenant(TENANT_ID)
      .jobId(linksChangeEvent.getJobId())
      .instanceId(null)
      .status(FAIL)
      .linkIds(emptyList())
      .failCause(failCause);
    sendKafkaMessage(linksInstanceAuthorityStatsTopic(), event.getJobId().toString(), event);

    assertLinksUpdated(failCause);
  }

  @Test
  @SneakyThrows
  void shouldHandleEvent_positive_whenLinkIdsAndInstanceIdEmpty() {
    var instanceId = UUID.fromString("8b400dd1-f328-4d46-ab94-7fb602232bfe");
    var authorityId = UUID.fromString("a501dcc2-23ce-4a4a-adb4-ff683b6f325e");
    //create inventory data to use for change event
    var inventoryEvent = new org.folio.entlinks.domain.dto.InventoryEvent()
      .id(authorityId)
      .tenant(TENANT_ID)
      .resourceName("test")
      .type("UPDATE")
      ._new(new AuthorityInventoryRecord().personalName("new personal name")
                                          .naturalId("1")
                                          .metadata(new AuthorityInventoryRecordMetadata()
                                                    .updatedByUserId(UUID.fromString(USER_ID))))
      .old(new AuthorityInventoryRecord().personalName("personal name").naturalId("2"));
    sendKafkaMessage(inventoryAuthorityTopic(), inventoryEvent.getId().toString(), inventoryEvent);

    awaitUntilAsserted(() -> assertEquals(1,
      databaseHelper.countRows(DatabaseHelper.AUTHORITY_DATA_STAT_TABLE, TENANT_ID)));
    doGet(authorityStatsEndpoint(UPDATE_HEADING, FROM_DATE, TO_DATE, 1))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0].headingOld", is("personal name")))
      .andExpect(jsonPath("$.stats[0].headingNew", is("new personal name")))
      .andExpect(jsonPath("$.stats[0].naturalIdOld", is("2")))
      .andExpect(jsonPath("$.stats[0].naturalIdNew", is("1")))
      .andExpect(jsonPath("$.stats[0].lbFailed", is(0)))
      .andExpect(jsonPath("$.stats[0].lbUpdated", is(0)))
      .andExpect(jsonPath("$.stats[0].lbTotal", is(0)))
      .andExpect(jsonPath("$.stats[0].metadata.startedAt", notNullValue()))
      .andExpect(jsonPath("$.stats[0].metadata.completedAt", notNullValue()));
  }

  private void prepareData(UUID instanceId, UUID authorityId, Link link) {
    // save link
    doPut(linksInstanceEndpoint(), linksDtoCollection(linksDto(instanceId, link)), instanceId);
    // prepare and send inventory update authority event to save stats data
    var authUpdateEvent = TestDataUtils.authorityEvent("UPDATE",
      new AuthorityInventoryRecord().id(authorityId).personalName("new personal name").naturalId("naturalId"),
      new AuthorityInventoryRecord().id(authorityId).personalName("personal name").naturalId("naturalId"));
    sendKafkaMessage(inventoryAuthorityTopic(), authorityId.toString(), authUpdateEvent);
  }

  @SneakyThrows
  private void assertLinksUpdated(String failCause) {
    ThreadUtils.sleep(Duration.ofSeconds(2));

    doGet(linksStatsInstanceEndpoint(LinkStatus.ERROR, OffsetDateTime.now().minusDays(1), OffsetDateTime.now()))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0].errorCause", is(failCause)));

    doGet(authorityStatsEndpoint(
      AuthorityDataStatActionDto.UPDATE_HEADING, OffsetDateTime.now().minusDays(1), OffsetDateTime.now(), 1))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0].lbFailed", is(1)))
      .andExpect(jsonPath("$.stats[0].lbFailed", is(1)))
      .andExpect(jsonPath("$.stats[0].lbUpdated", is(0)));
  }

  @Nullable
  @SneakyThrows
  private ConsumerRecord<String, LinksChangeEvent> getReceivedEvent() {
    return consumerRecords.poll(10, TimeUnit.SECONDS);
  }
}
