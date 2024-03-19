package org.folio.entlinks.service.consortium.propagation;

import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.folio.entlinks.service.consortium.ConsortiumTenantsService;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.springframework.scheduling.annotation.Async;

@Log4j2
public abstract class ConsortiumPropagationService<T> {

  private final ConsortiumTenantsService tenantsService;
  private final SystemUserScopedExecutionService executionService;

  protected ConsortiumPropagationService(ConsortiumTenantsService tenantsService,
                                         SystemUserScopedExecutionService executionService) {
    this.tenantsService = tenantsService;
    this.executionService = executionService;
  }

  @Async
  public void propagate(T entity, PropagationType propagationType,
                        String tenantId) {
    propagate(entity, propagationType, tenantId, false);
  }

  @Async
  public void propagate(T entity, PropagationType propagationType,
                        String tenantId, boolean publishRequired) {
    log.info("Try to propagate [entity: {}, propagationType: {}, context: {}]", entity.getClass().getSimpleName(),
      propagationType, tenantId);
    log.debug("Try to propagate [entity: {}, propagationType: {}, context: {}]", entity, propagationType, tenantId);
    try {
      var consortiumTenants = tenantsService.getConsortiumTenants(tenantId);
      log.debug("Find consortium tenants for propagation: {}, context: {}", consortiumTenants, tenantId);
      for (String consortiumTenant : consortiumTenants) {
        executionService.executeAsyncSystemUserScoped(consortiumTenant,
          () -> doPropagation(entity, propagationType, publishRequired));
      }
    } catch (FolioIntegrationException e) {
      log.warn("Skip propagation. Exception: ", e);
    }
  }

  protected abstract void doPropagation(T entity,
                                        PropagationType propagationType, boolean publishRequired);

  public enum PropagationType {
    CREATE, UPDATE, DELETE
  }
}
