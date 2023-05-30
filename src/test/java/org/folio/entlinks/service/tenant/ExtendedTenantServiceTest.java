package org.folio.entlinks.service.tenant;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.test.type.UnitTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.spring.tools.systemuser.PrepareSystemUserService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ExtendedTenantServiceTest {

  @InjectMocks
  private ExtendedTenantService tenantService;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private KafkaAdminService kafkaAdminService;
  @Mock
  private KafkaAdminClient kafkaAdminClient;
  @Mock
  private PrepareSystemUserService prepareSystemUserService;

  @Test
  void initializeTenant_positive() {
    when(context.getTenantId()).thenReturn(TENANT_ID);
    doNothing().when(prepareSystemUserService).setupSystemUser();
    doNothing().when(kafkaAdminService).createTopics(TENANT_ID);
    doNothing().when(kafkaAdminService).restartEventListeners();

    tenantService.afterTenantUpdate(tenantAttributes());

    verify(prepareSystemUserService).setupSystemUser();
    verify(kafkaAdminService).createTopics(TENANT_ID);
    verify(kafkaAdminService).restartEventListeners();
  }

  @Test
  void deleteTopicAfterTenantDeletion() {
    ListTopicsResult topicsResult = new ListTopicsResult(new KafkaFutureImpl<>());
    when(kafkaAdminClient.listTopics()).thenReturn(topicsResult);

    tenantService.afterTenantDeletion(tenantAttributes());

    verify(kafkaAdminService).deleteTopics(anyCollection());
  }

  private TenantAttributes tenantAttributes() {
    return new TenantAttributes().moduleTo("mod-entities-links");
  }
}
