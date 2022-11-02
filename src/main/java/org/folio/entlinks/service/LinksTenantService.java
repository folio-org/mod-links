package org.folio.entlinks.service;

import org.folio.qm.domain.dto.RecordType;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
public class LinksTenantService extends TenantService {

  private final LinkingRulesService rulesService;

  public LinksTenantService(JdbcTemplate jdbcTemplate,
                            FolioExecutionContext context,
                            LinkingRulesService rulesService,
                            FolioSpringLiquibase folioSpringLiquibase) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.rulesService = rulesService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    super.afterTenantUpdate(tenantAttributes);
    rulesService.saveDefaultRules(RecordType.AUTHORITY);
  }

}
