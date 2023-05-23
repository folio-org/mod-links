package org.folio.entlinks.controller.delegate;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.SearchClient;
import org.folio.entlinks.client.SourceStorageClient;
import org.folio.entlinks.controller.converter.DataMapper;
import org.folio.entlinks.domain.dto.ParsedRecordContentCollection;
import org.folio.entlinks.domain.dto.StrippedParsedRecordCollection;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.domain.repository.AuthorityDataRepository;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingRulesService;
import org.folio.entlinks.service.links.LinksSuggestionService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksSuggestionsServiceDelegate {

  private final InstanceAuthorityLinkingRulesService linkingRulesService;
  private final LinksSuggestionService suggestionService;
  private final AuthorityDataRepository dataRepository;
  private final SourceStorageClient sourceStorageClient;
  private final SearchClient searchClient;
  private final DataMapper dataMapper;

  public ParsedRecordContentCollection suggestLinksForMarcRecords(ParsedRecordContentCollection contentCollection) {
    var rules = rulesToBibFieldMap(linkingRulesService.getLinkingRules());
    var naturalIds = extractNaturalIdsOfLinkableFields(contentCollection, rules);
    var authorities = findAuthoritiesByNaturalIds(naturalIds);
    var marcAuthorities = fetchAuthorityParsedRecords(authorities);

    suggestionService
      .fillLinkDetailsWithSuggestedAuthorities(contentCollection, marcAuthorities, authorities, rules);

    return contentCollection;
  }

  private List<AuthorityData> findAuthoritiesByNaturalIds(Set<String> naturalIds) {
    var authorityData = dataRepository.findIdsByNaturalIds(naturalIds);
    if (authorityData.size() != naturalIds.size()) {
      authorityData.addAll(searchAndSaveAuthoritiesIds(naturalIds));
    }
    return authorityData;
  }

  private StrippedParsedRecordCollection fetchAuthorityParsedRecords(List<AuthorityData> authorityData) {
    if (nonNull(authorityData) && !authorityData.isEmpty()) {
      var ids = authorityData.stream().map(AuthorityData::getId).collect(Collectors.toSet());
      var authorityFetchRequest = sourceStorageClient.buildBatchFetchRequestForAuthority(ids,
        linkingRulesService.getMinAuthorityField(),
        linkingRulesService.getMaxAuthorityField());

      return sourceStorageClient.fetchParsedRecordsInBatch(authorityFetchRequest);
    }
    return new StrippedParsedRecordCollection(Collections.emptyList(), 0);
  }

  private List<AuthorityData> searchAndSaveAuthoritiesIds(Set<String> naturalIds) {
    var query = searchClient.buildNaturalIdsQuery(naturalIds);

    var authorityData = searchClient.searchAuthorities(query, false)
      .getAuthorities().stream()
      .map(dataMapper::convertToData)
      .toList();

    return dataRepository.saveAll(authorityData);
  }

  private Set<String> extractNaturalIdsOfLinkableFields(ParsedRecordContentCollection contentCollection,
                                                        Map<String, List<InstanceAuthorityLinkingRule>> rules) {
    return contentCollection.getRecords().stream()
      .flatMap(bibRecord -> bibRecord.getFields().entrySet().stream())
      .filter(field -> nonNull(rules.get(field.getKey())))
      .map(field -> field.getValue().getLinkDetails().getNaturalId())
      .collect(Collectors.toSet());
  }

  private Map<String, List<InstanceAuthorityLinkingRule>> rulesToBibFieldMap(List<InstanceAuthorityLinkingRule> rules) {
    return rules.stream().collect(groupingBy(InstanceAuthorityLinkingRule::getBibField));
  }
}
