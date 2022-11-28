package org.folio.entlinks.service.authority;

import static org.folio.entlinks.config.constants.CacheNames.AUTHORITY_TAG_TO_FIELD_CACHE;
import static org.folio.entlinks.utils.CollectionsUtils.containsIgnoreCase;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.folio.entlinks.integration.internal.MappingRulesService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MappingRulesProcessingService {

  private final MappingRulesService mappingRulesService;

  @Cacheable(cacheNames = AUTHORITY_TAG_TO_FIELD_CACHE,
             key = "@folioExecutionContext.tenantId + ':' + #authorityChange.getFieldName()",
             unless = "#result.isEmpty()")
  public String getTagByAuthorityChange(AuthorityChange authorityChange) {
    var mappingRelations = mappingRulesService.getFieldTargetsMappingRelations();
    return mappingRelations.entrySet().stream()
      .filter(mappingRelation -> containsIgnoreCase(mappingRelation.getValue(), authorityChange.getFieldName()))
      .map(Map.Entry::getKey)
      .findFirst()
      .orElseThrow(() -> new FolioIntegrationException(
        "Mapping rules don't contain mapping [field: " + authorityChange.getFieldName() + "]"));
  }
}
