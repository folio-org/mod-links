package org.folio.entlinks.service.messaging.authority;

import static org.folio.entlinks.config.constants.CacheNames.AUTHORITY_TAG_TO_FIELD_CACHE;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.folio.entlinks.integration.internal.MappingRulesService;
import org.folio.entlinks.service.messaging.authority.model.AuthorityChange;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorityMappingRulesProcessingService {

  private final MappingRulesService mappingRulesService;

  @Cacheable(cacheNames = AUTHORITY_TAG_TO_FIELD_CACHE,
             key = "@folioExecutionContext.tenantId + ':' + #authorityChange.getFieldName()",
             unless = "#result.isEmpty()")
  public String getTagByAuthorityChange(AuthorityChange authorityChange) {
    var mappingRelations = mappingRulesService.getFieldTargetsMappingRelations();
    return mappingRelations.entrySet().stream()
      .filter(mappingRelation -> mappingRelation.getValue().contains(authorityChange.getFieldName()))
      .map(Map.Entry::getKey)
      .findFirst()
      .orElseThrow(() -> new FolioIntegrationException(
        "Mapping rules don't contain mapping [field: " + authorityChange.getFieldName() + "]"));
  }
}
