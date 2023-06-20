package org.folio.entlinks.service.links;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.integration.dto.AuthorityParsedContent;
import org.folio.entlinks.integration.dto.FieldParsedContent;
import org.springframework.stereotype.Service;

@Service
public class AuthorityRuleValidationService {

  public Set<AuthorityData> validateAuthorityData(Map<UUID, AuthorityData> mapOfAuthorityData,
                                                  Map<UUID, String> authorityNaturalIds,
                                                  List<StrippedParsedRecord> authoritySources,
                                                  List<InstanceAuthorityLink> invalidLinks,
                                                  Map<UUID, List<InstanceAuthorityLink>> linksByAuthorityId) {
    return mapOfAuthorityData.values().stream()
      .map(authorityData -> {
        var authorityId = authorityData.getId();
        var naturalId = authorityNaturalIds.get(authorityId);
        var authority = authoritySources.stream()
          .filter(authorityRecord -> authorityRecord.getExternalIdsHolder().getAuthorityId().equals(authorityId))
          .findFirst();

        if (isNull(naturalId) || authority.isEmpty()) {
          invalidLinks.addAll(linksByAuthorityId.remove(authorityId));
          return null;
        }

        var authorityLinks = linksByAuthorityId.get(authorityId);
        var invalidLinksForAuthority = removeValidAuthorityLinks(authority.get(), authorityLinks);

        if (!invalidLinksForAuthority.isEmpty()) {
          invalidLinks.addAll(invalidLinksForAuthority);
          authorityLinks.removeAll(invalidLinksForAuthority);
        }
        if (invalidLinksForAuthority.size() == authorityLinks.size()) {
          return null;
        }

        authorityData.setNaturalId(naturalId);
        return authorityData;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  public boolean validateAuthorityFields(AuthorityParsedContent authorityContent, InstanceAuthorityLinkingRule rule) {
    var authorityFields = authorityContent.getFields().get(rule.getAuthorityField());

    if (nonNull(authorityFields) && authorityFields.size() == 1) {
      var authorityField = authorityFields.get(0);
      return validateAuthoritySubfieldsExistence(authorityField, rule);
    }
    return false;
  }

  public boolean validateAuthorityFields(StrippedParsedRecord authority, InstanceAuthorityLinkingRule rule) {
    var authorityFields = authority.getParsedRecord().getContent().getFields();

    if (isNotEmpty(authorityFields)) {
      var authorityField = authorityFields.stream()
        .flatMap(fields -> fields.entrySet().stream())
        .filter(field -> rule.getAuthorityField().equals(field.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();

      if (authorityField.isPresent()) {
        return validateAuthoritySubfieldsExistence(authorityField.get(), rule);
      }
    }
    return false;
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
    Predicate<String> containsTag = authoritySubfields::containsKey;

    return validateAuthoritySubfieldsExistence(rule, containsTag);
  }

  private boolean validateAuthoritySubfieldsExistence(FieldContent authorityField, InstanceAuthorityLinkingRule rule) {
    var authoritySubfields = authorityField.getSubfields();
    Predicate<String> containsTag = tag -> authoritySubfields.stream()
      .anyMatch(subfields -> subfields.containsKey(tag));

    return validateAuthoritySubfieldsExistence(rule, containsTag);
  }

  private boolean validateAuthoritySubfieldsExistence(InstanceAuthorityLinkingRule rule, Predicate<String> contains) {
    var existValidation = rule.getSubfieldsExistenceValidations();
    if (isNotEmpty(existValidation)) {
      for (var subfieldExistence : existValidation.entrySet()) {
        var containsTag = contains.test(subfieldExistence.getKey());
        if (containsTag != subfieldExistence.getValue()) {
          return false;
        }
      }
    }
    return true;
  }
}
