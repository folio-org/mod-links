package org.folio.entlinks.service.links;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstanceAuthorityLinkingRulesService {

  private final LinkingRulesRepository repository;

  public List<InstanceAuthorityLinkingRule> getLinkingRules() {
    return repository.findAll();
  }

  public List<InstanceAuthorityLinkingRule> getLinkingRulesByAuthorityField(String authorityField) {
    return repository.findByAuthorityField(authorityField);
  }

}
