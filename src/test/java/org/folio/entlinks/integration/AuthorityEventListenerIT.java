package org.folio.entlinks.integration;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.ONE_MINUTE;
import static org.awaitility.Durations.ONE_SECOND;
import static org.folio.support.TestUtils.linksDto;
import static org.folio.support.TestUtils.linksDtoCollection;
import static org.folio.support.base.TestConstants.inventoryAuthorityTopic;
import static org.folio.support.base.TestConstants.linksInstanceEndpoint;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.folio.entlinks.service.AuthorityChangeHandlingService;
import org.folio.qm.domain.dto.AuthorityInventoryRecord;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.support.TestUtils;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;

@IntegrationTest
class AuthorityEventListenerIT extends IntegrationTestBase {

  @MockBean
  private AuthorityChangeHandlingService authorityChangeHandlingService;

  @ValueSource(strings = {"DELETE", "UPDATE"})
  @ParameterizedTest
  void shouldHandleEvent_positive_whenAuthorityLinkExistAndChangeIsFound(String eventType) {
    var instanceId = UUID.randomUUID();
    var link = TestUtils.Link.of(0, 0);
    var incomingLinks = linksDtoCollection(linksDto(instanceId, link));
    doPut(linksInstanceEndpoint(), incomingLinks, instanceId);

    var event = TestUtils.authorityEvent(eventType,
      new AuthorityInventoryRecord().id(link.authorityId()),
      new AuthorityInventoryRecord().id(link.authorityId()).naturalId("12345"));
    sendKafkaMessage(inventoryAuthorityTopic(), event);

    await().atMost(ONE_MINUTE).pollInterval(ONE_SECOND).untilAsserted(() ->
      verify(authorityChangeHandlingService).processAuthoritiesChanges(List.of(event)));
  }

  @ValueSource(strings = {"DELETE", "UPDATE"})
  @ParameterizedTest
  void shouldNotHandleEvent_positive_whenAuthorityLinksNotExist(String eventType) {
    var authorityId = UUID.randomUUID();
    var event = TestUtils.authorityEvent(eventType,
      new AuthorityInventoryRecord().id(authorityId),
      new AuthorityInventoryRecord().id(authorityId).naturalId("12345"));
    sendKafkaMessage(inventoryAuthorityTopic(), event);

    await().atMost(ONE_MINUTE).pollInterval(ONE_SECOND).untilAsserted(() ->
      verify(authorityChangeHandlingService, never()).processAuthoritiesChanges(List.of(event)));
  }

  @ValueSource(strings = {"CREATE", "REINDEX", "ITERATE"})
  @ParameterizedTest
  void shouldNotHandleEvent_positive_whenEventIsNotRelatedToChanges(String eventType) {
    var authorityId = UUID.randomUUID();
    var event = TestUtils.authorityEvent(eventType,
      new AuthorityInventoryRecord().id(authorityId),
      new AuthorityInventoryRecord().id(authorityId).naturalId("12345"));
    sendKafkaMessage(inventoryAuthorityTopic(), event);

    await().atMost(ONE_MINUTE).pollInterval(ONE_SECOND).untilAsserted(() ->
      verify(authorityChangeHandlingService, never()).processAuthoritiesChanges(List.of(event)));
  }

  @Test
  void shouldNotHandleEvent_positive_whenUpdateEventWithEqualOldAndNew() {
    var authorityId = UUID.randomUUID();
    var event = TestUtils.authorityEvent("UPDATE",
      new AuthorityInventoryRecord().id(authorityId),
      new AuthorityInventoryRecord().id(authorityId));
    sendKafkaMessage(inventoryAuthorityTopic(), event);

    await().atMost(ONE_MINUTE).pollInterval(ONE_HUNDRED_MILLISECONDS).untilAsserted(() ->
      verify(authorityChangeHandlingService, never()).processAuthoritiesChanges(List.of(event)));
  }

}
