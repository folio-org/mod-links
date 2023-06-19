package org.folio.entlinks.service.links;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.springframework.stereotype.Service;

@Service
public class AuthorityDataValidationService {

  public Set<AuthorityData> validateAuthorityData(Map<UUID, AuthorityData> mapOfAuthorityData,
                                                  Map<UUID, String> authorityNaturalIds,
                                                  List<StrippedParsedRecord> authoritySources,
                                                  List<InstanceAuthorityLink> invalidLinks,
                                                  Map<UUID, List<InstanceAuthorityLink>> linksByAuthorityId) {
    return mapOfAuthorityData.values().stream()
      .map(authorityData ->
        validateAuthorityData(authorityData, authorityNaturalIds, authoritySources, invalidLinks, linksByAuthorityId))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  private AuthorityData validateAuthorityData(AuthorityData authorityData,
                                              Map<UUID, String> authorityNaturalIds,
                                              List<StrippedParsedRecord> authoritySources,
                                              List<InstanceAuthorityLink> invalidLinks,
                                              Map<UUID, List<InstanceAuthorityLink>> linksByAuthorityId) {
    var authorityId = authorityData.getId();
    var naturalId = authorityNaturalIds.get(authorityId);
    var authority = authoritySources.stream()
      .filter(authorityRecord -> authorityRecord.getExternalIdsHolder().getAuthorityId().equals(authorityId))
      .findFirst();

    if (isNull(naturalId) || authority.isEmpty()) {
      invalidLinks.addAll(linksByAuthorityId.remove(authorityId));
      return null;
    }

    var authorityFields = authority.get().getParsedRecord().getContent().getFields();
    var authorityLinks = linksByAuthorityId.get(authorityId);
    var invalidLinksForAuthority = authorityLinks.stream()
      .filter(link -> !isLinkValid(authorityFields, link))
      .toList();
    if (!invalidLinksForAuthority.isEmpty()) {
      invalidLinks.addAll(invalidLinksForAuthority);
      authorityLinks.removeAll(invalidLinksForAuthority);
    }
    if (invalidLinksForAuthority.size() == authorityLinks.size()) {
      return null;
    }

    authorityData.setNaturalId(naturalId);
    return authorityData;
  }

  private boolean isLinkValid(List<Map<String, FieldContent>> authorityFields, InstanceAuthorityLink link) {
    return authorityFields.stream()
      .flatMap(fields -> fields.entrySet().stream())
      .filter(field -> link.getLinkingRule().getAuthorityField().equals(field.getKey()))
      .anyMatch(field -> isSubfieldsValid(field.getValue(), link.getLinkingRule()));
  }

  private boolean isSubfieldsValid(FieldContent authorityField, InstanceAuthorityLinkingRule linkingRule) {
    var existValidation = linkingRule.getSubfieldsExistenceValidations();
    if (isNotEmpty(existValidation)) {
      var authoritySubfields = authorityField.getSubfields();

      for (var subfieldExistence : existValidation.entrySet()) {
        var contains = authoritySubfields.stream()
          .anyMatch(subfieldMap -> subfieldMap.containsKey(subfieldExistence.getKey()));
        if (contains != subfieldExistence.getValue()) {
          return false;
        }
      }
    }
    return true;
  }
}
