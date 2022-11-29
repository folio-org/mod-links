package org.folio.entlinks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.model.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private final LinkingRulesRepository repository;

  public List<InstanceAuthorityLinkingRule> getLinkingRules() {
    return repository.findAll();
  }

  public List<InstanceAuthorityLinkingRule> getLinkingRuleForAuthorityField(String authorityField) {
    return repository.findByAuthorityField(authorityField);
  }

}
