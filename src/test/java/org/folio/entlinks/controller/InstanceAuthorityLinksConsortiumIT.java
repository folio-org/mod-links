package org.folio.entlinks.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.DatabaseHelper.AUTHORITY_TABLE;
import static org.folio.support.KafkaTestUtils.createAndStartTestConsumer;
import static org.folio.support.TestDataUtils.linksDto;
import static org.folio.support.TestDataUtils.linksDtoCollection;
import static org.folio.support.base.TestConstants.CENTRAL_TENANT_ID;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_1;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_10;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_11;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_2;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_3;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_4;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_5;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_6;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_7;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_8;
import static org.folio.support.base.TestConstants.MEMBER_TENANT_9;
import static org.folio.support.base.TestConstants.authorityEndpoint;
import static org.folio.support.base.TestConstants.authorityTopic;
import static org.folio.support.base.TestConstants.linksInstanceEndpoint;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.dto.InstanceLinkDto;
import org.folio.entlinks.domain.dto.InstanceLinkDtoCollection;
import org.folio.entlinks.integration.dto.event.AuthorityDomainEvent;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.DatabaseHelper;
import org.folio.support.TestDataUtils;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

@IntegrationTest
@DatabaseCleanup(tables = {
  DatabaseHelper.INSTANCE_AUTHORITY_LINK_TABLE,
  DatabaseHelper.AUTHORITY_TABLE},
    tenants = {CENTRAL_TENANT_ID, MEMBER_TENANT_1, MEMBER_TENANT_2, MEMBER_TENANT_3, MEMBER_TENANT_4, MEMBER_TENANT_5,
      MEMBER_TENANT_6, MEMBER_TENANT_7, MEMBER_TENANT_8, MEMBER_TENANT_9, MEMBER_TENANT_10, MEMBER_TENANT_11})
public class InstanceAuthorityLinksConsortiumIT extends IntegrationTestBase {

  public static final List<String> MEMBER_TENANTS = List.of(MEMBER_TENANT_1, MEMBER_TENANT_2, MEMBER_TENANT_3,
      MEMBER_TENANT_4, MEMBER_TENANT_5, MEMBER_TENANT_6, MEMBER_TENANT_7, MEMBER_TENANT_8, MEMBER_TENANT_9,
      MEMBER_TENANT_10, MEMBER_TENANT_11);
  private KafkaMessageListenerContainer<String, AuthorityDomainEvent> container;
  private BlockingQueue<ConsumerRecord<String, AuthorityDomainEvent>> consumerRecords;

  @BeforeAll
  static void prepare() {
    setUpConsortium(CENTRAL_TENANT_ID, MEMBER_TENANTS, true);
  }

  @BeforeEach
  void setup(@Autowired KafkaProperties kafkaProperties) {
    consumerRecords = new LinkedBlockingQueue<>();
    container = createAndStartTestConsumer(
        authorityTopic(), consumerRecords, kafkaProperties, AuthorityDomainEvent.class);
  }

  @AfterEach
  void tearDown() {
    consumerRecords.clear();
    container.stop();
  }

  @Test
  @SneakyThrows
  void shouldNotChangeAuthorityVersionAfterInstanceLinksUpdateTwice() {
    var instanceId = randomUUID();
    var existedLinks = createLinkDtoCollection(instanceId);
    createAuthorityForConsortium(existedLinks.getLinks().get(0));

    awaitUntilAsserted(() -> {
      MEMBER_TENANTS.forEach(tenantId -> {
        assertEquals(1, databaseHelper.countRows(AUTHORITY_TABLE, tenantId));
      });
      assertEquals(1, databaseHelper.countRows(AUTHORITY_TABLE, CENTRAL_TENANT_ID));
    });

    doPut(linksInstanceEndpoint(), existedLinks, tenantHeaders(CENTRAL_TENANT_ID), instanceId);
    doPut(linksInstanceEndpoint(), existedLinks, tenantHeaders(CENTRAL_TENANT_ID), instanceId);

    awaitUntilAsserted(() -> {
      MEMBER_TENANTS.forEach(tenantId -> {
        assertEquals(0, databaseHelper.queryAuthorityVersion(
            tenantId, existedLinks.getLinks().get(0).getAuthorityId()));
      });
      assertEquals(0, databaseHelper.queryAuthorityVersion(
          CENTRAL_TENANT_ID, existedLinks.getLinks().get(0).getAuthorityId()));
    });
  }

  private InstanceLinkDtoCollection createLinkDtoCollection(UUID instanceId) {
    var links = IntStream.of(1)
        .mapToObj(i -> TestDataUtils.Link.of(i, i, TestDataUtils.NATURAL_IDS[i]))
        .toArray(TestDataUtils.Link[]::new);
    return linksDtoCollection(linksDto(instanceId, links));
  }

  private void createAuthorityForConsortium(InstanceLinkDto link) {
    var dto = new AuthorityDto()
        .id(link.getAuthorityId())
        .version(0)
        .source("MARC")
        .naturalId(link.getAuthorityNaturalId())
        .personalName("Nikola Tesla1");
    doPost(authorityEndpoint(), dto, tenantHeaders(CENTRAL_TENANT_ID));
  }
}
