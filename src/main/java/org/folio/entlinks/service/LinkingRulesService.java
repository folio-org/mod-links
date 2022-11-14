package org.folio.entlinks.service;

import lombok.RequiredArgsConstructor;
import org.folio.entlinks.LinkingPairType;
import org.folio.entlinks.model.converter.LinkingRulesMapper;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private final LinkingRulesRepository repository;
  private final LinkingRulesMapper mapper;

  public List<LinkingRuleDto> getLinkingRules(LinkingPairType linkingPairType) {
    var jsonRules = repository.findByLinkingPairType(linkingPairType.name());
    return mapper.convert(jsonRules);
  }
}
