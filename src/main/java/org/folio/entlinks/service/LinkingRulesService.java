package org.folio.entlinks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.LinkingPairType;
import org.folio.entlinks.model.converter.LinkingRulesMapper;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private final LinkingRulesRepository repository;
  private final LinkingRulesMapper mapper;

  public List<LinkingRuleDto> getLinkingRules(LinkingPairType linkingPairType) {
    var jsonRules = repository.findByLinkingPairType(linkingPairType.name());
    return mapper.convert(jsonRules);
  }

  public List<LinkingRuleDto> getLinkingRuleForAuthorityField(String authorityField) {
    return getLinkingRules(LinkingPairType.INSTANCE_AUTHORITY).stream()
      .filter(linkingRuleDto -> linkingRuleDto.getAuthorityField().equals(authorityField))
      .toList();
  }

  public LinkingRuleDto getLinkingRuleForBibField(String bibField) {
    return getLinkingRules(LinkingPairType.INSTANCE_AUTHORITY).stream()
      .filter(linkingRuleDto -> linkingRuleDto.getBibField().equals(bibField))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No linking rules for [bib field: " + bibField + "]"));
  }
}
