package org.folio.entlinks.service.links;

import static java.util.Objects.nonNull;
import static org.folio.entlinks.domain.dto.LinkStatus.ACTUAL;
import static org.folio.entlinks.domain.dto.LinkStatus.ERROR;
import static org.folio.entlinks.domain.dto.LinkStatus.NEW;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.LinkDetails;
import org.folio.entlinks.domain.dto.ParsedRecordContent;
import org.folio.entlinks.domain.dto.ParsedRecordContentCollection;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.dto.StrippedParsedRecordCollection;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksSuggestionService {
  private static final String NO_SUGGESTIONS_ERROR_CODE = "101";
  private static final String MORE_THEN_ONE_SUGGESTIONS_ERROR_CODE = "102";

  /**
   * Validate bib-authority fields by linking rules and fill bib fields with suggested links.
   *
   * @param bibs          collection of bib records {@link ParsedRecordContent}
   * @param authorities   collection of authorities {@link StrippedParsedRecord} that can be suggested as link
   * @param authorityData collection of authoritiesData to retrieve naturalIds
   * @param rules         linking rules
   *                      <p>Key - {@link String} as bib tag, Value - list of {@link InstanceAuthorityLinkingRule}</p>
   */
  public void fillLinkDetailsWithSuggestedAuthorities(ParsedRecordContentCollection bibs,
                                                      StrippedParsedRecordCollection authorities,
                                                      List<AuthorityData> authorityData,
                                                      Map<String, List<InstanceAuthorityLinkingRule>> rules) {
    if (nonNull(authorities) && !authorities.getRecords().isEmpty()) {
      bibs.getRecords().stream()
        .flatMap(bib -> bib.getFields().entrySet().stream())
        .forEach(bibField -> suggestAuthorityForBibField(
          bibField.getValue(),
          authorities.getRecords(),
          authorityData,
          rules.get(bibField.getKey())));
    }
  }

  private void suggestAuthorityForBibField(FieldContent bibField,
                                           List<StrippedParsedRecord> authorities,
                                           List<AuthorityData> authorityData,
                                           List<InstanceAuthorityLinkingRule> rules) {
    for (var rule : rules) {
      if (rule.getAutoLinkingEnabled()) {
        var suitableAuthorities = authorities.stream()
          .filter(authority -> validateAuthorityFields(authority, rule))
          .toList();

        if (suitableAuthorities.isEmpty()) {
          var errorDetails = getErrorDetails(NO_SUGGESTIONS_ERROR_CODE);
          bibField.setLinkDetails(errorDetails);
        } else if (suitableAuthorities.size() > 1) {
          var errorDetails = getErrorDetails(MORE_THEN_ONE_SUGGESTIONS_ERROR_CODE);
          bibField.setLinkDetails(errorDetails);
        } else {
          var authority = suitableAuthorities.get(0);
          var naturalId = extractNaturalId(authorityData, authority.getId());
          var linkDetails = getLinkDetails(bibField, authority, naturalId, rule);
          actualizeBibSubfields(bibField, authority, naturalId, rule);
          bibField.setLinkDetails(linkDetails);
        }
      }
    }
  }

  private LinkDetails getErrorDetails(String errorCode) {
    return new LinkDetails().linksStatus(ERROR).errorStatusCode(errorCode);
  }

  private LinkDetails getLinkDetails(FieldContent bibField,
                                     StrippedParsedRecord authority, String naturalId,
                                     InstanceAuthorityLinkingRule rule) {
    var linkDetails = bibField.getLinkDetails();

    if (linkDetails == null) {
      linkDetails = new LinkDetails();
      linkDetails.setLinksStatus(NEW);
    } else {
      linkDetails.setLinksStatus(ACTUAL);
    }
    linkDetails.setRuleId(rule.getId());
    linkDetails.setAuthorityId(authority.getId());
    linkDetails.setNaturalId(naturalId);
    return linkDetails;
  }

  private void actualizeBibSubfields(FieldContent bibField,
                                     StrippedParsedRecord authority, String naturalId,
                                     InstanceAuthorityLinkingRule rule) {
    var bibSubfields = bibField.getSubfields();
    var authoritySubfields = authority.getParsedRecord().getContent().getFields()
      .get(rule.getAuthorityField())
      .getSubfields();

    bibSubfields.putAll(authoritySubfields);
    bibSubfields.put("0", naturalId);
    bibSubfields.put("9", authority.getId().toString());
  }

  private boolean validateAuthorityFields(StrippedParsedRecord authority, InstanceAuthorityLinkingRule rule) {
    var fields = authority.getParsedRecord().getContent().getFields();
    var authorityField = fields.get(rule.getAuthorityField());

    if (nonNull(authorityField)) {
      return validateAuthoritySubfields(authorityField, rule);
    }
    return false;
  }

  private boolean validateAuthoritySubfields(FieldContent authorityField,
                                             InstanceAuthorityLinkingRule rule) {
    var existValidation = rule.getSubfieldsExistenceValidations();
    if (!existValidation.isEmpty()) {
      var authoritySubfields = authorityField.getSubfields();

      for (var subfieldExistence : existValidation.entrySet()) {
        var contains = authoritySubfields.containsKey(subfieldExistence.getKey());
        if (contains != subfieldExistence.getValue()) {
          return false;
        }
      }
    }
    return true;
  }

  private String extractNaturalId(List<AuthorityData> authorityData, UUID authorityId) {
    return authorityData.stream()
      .filter(data -> data.getId().equals(authorityId))
      .map(AuthorityData::getNaturalId)
      .findAny()
      .orElse(null);
  }
}
