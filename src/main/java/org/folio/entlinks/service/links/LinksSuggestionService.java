package org.folio.entlinks.service.links;

import static java.util.Objects.nonNull;
import static org.folio.entlinks.domain.dto.LinkStatus.ACTUAL;
import static org.folio.entlinks.domain.dto.LinkStatus.ERROR;
import static org.folio.entlinks.domain.dto.LinkStatus.NEW;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.ParsedRecordContent;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
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
   * @param bibs        list of bib records
   * @param authorities list of authorities that can be suggested as link for bib fields
   * @param rules       linking rules
   *                    <p>Key - {@link String} as bib tag, Value - list of {@link InstanceAuthorityLinkingRule}</p>
   */
  public void fillLinkDetailsWithSuggestedAuthorities(List<ParsedRecordContent> bibs,
                                                      List<StrippedParsedRecord> authorities,
                                                      Map<String, List<InstanceAuthorityLinkingRule>> rules) {
    bibs.stream()
      .flatMap(bib -> bib.getFields().entrySet().stream())
      .forEach(bibField -> suggestAuthorityForBibField(bibField.getValue(), authorities, rules.get(bibField.getKey())));
  }

  private void suggestAuthorityForBibField(FieldContent bibField,
                                           List<StrippedParsedRecord> authorities,
                                           List<InstanceAuthorityLinkingRule> rules) {
    for (var rule : rules) {
      if (rule.getAutoLinkingEnabled()) {
        var suitableAuthorities = authorities.stream()
          .filter(authority -> validateAuthorityFields(authority, rule))
          .toList();

        if (suitableAuthorities.isEmpty()) {
          fillErrorDetails(bibField, NO_SUGGESTIONS_ERROR_CODE);
        } else if (suitableAuthorities.size() > 1) {
          fillErrorDetails(bibField, MORE_THEN_ONE_SUGGESTIONS_ERROR_CODE);
        } else {
          fillLinkDetails(bibField, suitableAuthorities.get(0), rule);
        }
      }
    }
  }

  private void fillErrorDetails(FieldContent bibField, String errorCode) {
    bibField.getLinkDetails().setLinksStatus(ERROR);
    bibField.getLinkDetails().setErrorStatusCode(errorCode);
  }

  private void fillLinkDetails(FieldContent bibField,
                               StrippedParsedRecord authority,
                               InstanceAuthorityLinkingRule rule) {
    if (bibField.getLinkDetails().getRuleId() != null) {
      bibField.getLinkDetails().setLinksStatus(ACTUAL);
    } else {
      bibField.getLinkDetails().setLinksStatus(NEW);
    }
    actualizeBibSubfields(bibField, authority, rule);
    bibField.getLinkDetails().setRuleId(rule.getId());
    bibField.getLinkDetails().setAuthorityId(authority.getId());
    //TODO: set naturalId from mod-search OR make SRS returns naturalId
  }

  private void actualizeBibSubfields(FieldContent bibField,
                                     StrippedParsedRecord authority,
                                     InstanceAuthorityLinkingRule rule) {
    var bibSubfields = bibField.getSubfields();
    var authoritySubfields = authority.getParsedRecord().getContent().getFields()
      .get(rule.getAuthorityField())
      .getSubfields();

    bibSubfields.putAll(authoritySubfields);
    //bibSubfields.put("0", authority.getNaturalId());
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
}
