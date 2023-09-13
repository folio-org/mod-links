package org.folio.entlinks.service.consortium;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.client.ConsortiumTenantsClient;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsortiumTenantsService {

  private final UserTenantsService userTenantsService;
  private final ConsortiumTenantsClient consortiumTenantsClient;

  public List<String> getConsortiumTenants(String tenantId) {
    try {
      return userTenantsService.getConsortiumId(tenantId)
        .map(consortiumTenantsClient::getConsortiumTenants)
        .map(ConsortiumTenantsClient.ConsortiumTenants::tenants)
        .map(consortiumTenants -> consortiumTenants.stream().map(ConsortiumTenantsClient.ConsortiumTenant::id).toList())
        .orElseThrow(() -> new FolioIntegrationException("Consortium is not enabled for the tenant=" + tenantId));
    } catch (Exception e) {
      throw new FolioIntegrationException("Unexpected exception occurred while trying to get consortium tenants", e);
    }
  }
}
