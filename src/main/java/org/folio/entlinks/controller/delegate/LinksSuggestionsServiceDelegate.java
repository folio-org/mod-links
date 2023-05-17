package org.folio.entlinks.controller.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.dto.SrsRecordsContentCollection;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingRulesService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksSuggestionsServiceDelegate {

  private final InstanceAuthorityLinkingRulesService linkingRulesService;

  public SrsRecordsContentCollection suggestLinksForMarcRecord(
    SrsRecordsContentCollection srsRecordsContentCollection) {
    return null;
  }
}
