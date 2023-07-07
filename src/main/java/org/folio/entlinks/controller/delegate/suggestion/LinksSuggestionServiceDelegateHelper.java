package org.folio.entlinks.controller.delegate.suggestion;

import java.util.Map;
import org.folio.entlinks.domain.dto.AuthoritySearchParameter;
import org.springframework.stereotype.Service;

@Service
public class LinksSuggestionServiceDelegateHelper {

  private final Map<AuthoritySearchParameter, LinksSuggestionServiceDelegate> searchParameterToServiceDelegateMap;

  public LinksSuggestionServiceDelegateHelper(
    LinksSuggestionsByAuthorityNaturalId linksSuggestionsByAuthorityNaturalId,
    LinksSuggestionsByAuthorityId linksSuggestionsByAuthorityId) {

    searchParameterToServiceDelegateMap = Map.of(
      AuthoritySearchParameter.NATURAL_ID, linksSuggestionsByAuthorityNaturalId,
      AuthoritySearchParameter.ID, linksSuggestionsByAuthorityId
    );
  }

  public LinksSuggestionServiceDelegate getDelegate(AuthoritySearchParameter authoritySearchParameter) {
    return searchParameterToServiceDelegateMap.get(authoritySearchParameter);
  }

}
