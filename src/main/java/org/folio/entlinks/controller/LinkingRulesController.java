package org.folio.entlinks.controller;

import lombok.RequiredArgsConstructor;
import org.folio.entlinks.service.LinkingRulesService;
import org.folio.qm.domain.dto.RecordType;
import org.folio.qm.rest.resource.LinkingRulesApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LinkingRulesController implements LinkingRulesApi {

  private final LinkingRulesService linkingRulesService;

  @Override
  public ResponseEntity<String> getAuthorityLinkingRules(RecordType recordType) {
    var rules = linkingRulesService.getLinkingRules(recordType);
    return ResponseEntity.ok(rules);
  }
}
