package org.folio.entlinks.service.links;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.integration.dto.AuthorityParsedContent;
import org.folio.entlinks.integration.dto.FieldParsedContent;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AuthorityRuleValidationService {

  public Set<AuthorityData> validateAuthorityData(Map<UUID, AuthorityData> mapOfAuthorityData,
                                                  Map<UUID, String> authorityNaturalIds,
                                                  List<StrippedParsedRecord> authoritySources,
                                                  List<InstanceAuthorityLink> invalidLinks,
                                                  Map<UUID, List<InstanceAuthorityLink>> linksByAuthorityId) {
    Set<AuthorityData> validAuthorityData = new HashSet<>();

    for (AuthorityData authorityData : mapOfAuthorityData.values()) {
      var authorityId = authorityData.getId();
      var naturalId = authorityNaturalIds.get(authorityId);
      var authority = authoritySources.stream()
        .filter(authorityRecord -> authorityRecord.getExternalIdsHolder().getAuthorityId().equals(authorityId))
        .findFirst();

      if (isNull(naturalId) || authority.isEmpty()) {
        invalidLinks.addAll(linksByAuthorityId.remove(authorityId));
        continue;
      }

      var authorityLinks = linksByAuthorityId.get(authorityId);
      var invalidLinksForAuthority = removeValidAuthorityLinks(authority.get(), authorityLinks);

      if (!invalidLinksForAuthority.isEmpty()) {
        invalidLinks.addAll(invalidLinksForAuthority);
        authorityLinks.removeAll(invalidLinksForAuthority);
      }
      if (invalidLinksForAuthority.size() == authorityLinks.size()) {
        continue;
      }

      authorityData.setNaturalId(naturalId);
      validAuthorityData.add(authorityData);
    }
    return validAuthorityData;
  }

  public boolean validateAuthorityFields(AuthorityParsedContent authorityContent, InstanceAuthorityLinkingRule rule) {
    log.info("Starting validation for authority {}", authorityContent.getId());
    var authorityFields = authorityContent.getFields().get(rule.getAuthorityField());

    if (validateAuthorityFields(authorityFields)) {
      var authorityField = authorityFields.get(0);
      return validateAuthoritySubfieldsExistence(authorityField, rule);
    }
    return false;
  }

  public boolean validateAuthorityFields(StrippedParsedRecord authority, InstanceAuthorityLinkingRule rule) {
    log.info("Starting validation for authority {}", authority.getId());
    var authorityFields = authority.getParsedRecord().getContent().getFields().stream()
      .flatMap(fields -> fields.entrySet().stream())
      .filter(field -> rule.getAuthorityField().equals(field.getKey()))
      .map(Map.Entry::getValue)
      .toList();

    if (validateAuthorityFields(authorityFields)) {
      var authorityField = authorityFields.get(0);
      return validateAuthoritySubfieldsExistence(authorityField, rule);
    }
    return false;
  }

  private boolean validateAuthorityFields(List<?> authorityFields) {
    if (isEmpty(authorityFields)) {
      log.warn("Validation failed: Authority does not contains linkable field");
      return false;
    }
    if (authorityFields.size() > 1) {
      log.warn("Validation failed: Authority contains more than one linkable fields");
      return false;
    }
    return true;
  }

  private List<InstanceAuthorityLink> removeValidAuthorityLinks(StrippedParsedRecord authority,
                                                                List<InstanceAuthorityLink> authorityLinks) {
    return authorityLinks.stream()
      .filter(link -> !validateAuthorityFields(authority, link.getLinkingRule()))
      .toList();
  }

  private boolean validateAuthoritySubfieldsExistence(FieldParsedContent authorityField,
                                                      InstanceAuthorityLinkingRule rule) {
    var authoritySubfields = authorityField.getSubfields();
    Predicate<String> containsSubfield = authoritySubfields::containsKey;

    return validateAuthoritySubfieldsExistence(rule, containsSubfield);
  }

  private boolean validateAuthoritySubfieldsExistence(FieldContent authorityField, InstanceAuthorityLinkingRule rule) {
    var authoritySubfields = authorityField.getSubfields();
    Predicate<String> containsSubfield = subfield -> authoritySubfields.stream()
      .anyMatch(subfields -> subfields.containsKey(subfield));

    return validateAuthoritySubfieldsExistence(rule, containsSubfield);
  }

  private boolean validateAuthoritySubfieldsExistence(InstanceAuthorityLinkingRule rule, Predicate<String> contains) {
    var existValidation = rule.getSubfieldsExistenceValidations();
    if (isNotEmpty(existValidation)) {
      for (var subfieldExistence : existValidation.entrySet()) {
        var subfield = subfieldExistence.getKey();
        var doesItContains = contains.test(subfield);
        var shouldItContains = subfieldExistence.getValue();

        if (doesItContains != shouldItContains) {
          logSubfieldExistenceValidationFailure(rule.getAuthorityField(), subfield, shouldItContains, doesItContains);
          return false;
        }
      }
    }
    return true;
  }

  private void logSubfieldExistenceValidationFailure(String authorityField, String subfield,
                                                     boolean shouldItContains, boolean doesItContains) {
    String shouldExist = shouldItContains ? "exist" : "not exist";
    String doesExist = doesItContains ? "does" : "does not";

    log.info("Subfield validation failed for authority field '{}'. Subfield '{}' should {}, but it {}",
      authorityField, subfield, shouldExist, doesExist);
  }
}
