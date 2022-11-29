package org.folio.entlinks.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.controller.converter.LinkingRulesMapper;
import org.folio.entlinks.service.LinkingRulesService;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.folio.qm.rest.resource.LinkingRulesApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LinkingRulesController implements LinkingRulesApi {

  private final LinkingRulesService linkingRulesService;
  private final LinkingRulesMapper mapper;

  @Override
  public ResponseEntity<List<LinkingRuleDto>> getInstanceAuthorityLinkingRules() {
    var rules = linkingRulesService.getLinkingRules();
    return ResponseEntity.ok(mapper.convert(rules));
  }
}
