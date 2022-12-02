package org.folio.entlinks.service.links;

import static org.folio.entlinks.config.constants.CacheNames.AUTHORITY_LINKING_RULES_CACHE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstanceAuthorityLinkingRulesService {

  private final LinkingRulesRepository repository;

  public List<InstanceAuthorityLinkingRule> getLinkingRules() {
    return repository.findAll();
  }

  @Cacheable(cacheNames = AUTHORITY_LINKING_RULES_CACHE,
             key = "@folioExecutionContext.tenantId + ':' + #authorityField", unless = "#result.isEmpty()")
  public List<InstanceAuthorityLinkingRule> getLinkingRulesByAuthorityField(String authorityField) {
    return repository.findByAuthorityField(authorityField);
  }

}
