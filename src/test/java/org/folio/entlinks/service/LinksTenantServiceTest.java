package org.folio.entlinks.service;

import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.qm.domain.dto.RecordType;
import org.folio.spring.FolioExecutionContext;
import org.folio.support.types.UnitTest;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinksTenantServiceTest {

  @InjectMocks
  private LinksTenantService tenantService;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private LinkingRulesService rulesService;

  @Test
  void initializeTenant_positive() {
    when(context.getTenantId()).thenReturn(TENANT_ID);
    doNothing().when(rulesService).saveDefaultRules(RecordType.AUTHORITY);

    tenantService.afterTenantUpdate(tenantAttributes());

    verify(rulesService).saveDefaultRules(RecordType.AUTHORITY);
  }

  private TenantAttributes tenantAttributes() {
    return new TenantAttributes().moduleTo("mod-entities-links");
  }
}
