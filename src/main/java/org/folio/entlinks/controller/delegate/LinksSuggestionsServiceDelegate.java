package org.folio.entlinks.controller.delegate;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.SearchClient;
import org.folio.entlinks.client.SourceStorageClient;
import org.folio.entlinks.controller.converter.DataMapper;
import org.folio.entlinks.domain.dto.ParsedRecordContent;
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
  private final AuthorityDataRepository repository;
  private final SourceStorageClient sourceStorageClient;
  private final SearchClient searchClient;
  private final DataMapper dataMapper;

  public ParsedRecordContentCollection suggestLinksForMarcRecord(ParsedRecordContentCollection contentCollection) {
    var parsedRecords = contentCollection.getRecords();
    var rules = rulesToBibFieldMap(linkingRulesService.getLinkingRules());
    var naturalIds = extractNaturalIdsOfLinkableFields(parsedRecords, rules);
    var authorities = fetchAuthorityParsedRecords(naturalIds);

    suggestionService.fillLinkDetailsWithSuggestedAuthorities(parsedRecords, authorities.getRecords(), rules);

    return contentCollection;
  }

  private StrippedParsedRecordCollection fetchAuthorityParsedRecords(Set<String> naturalIds) {
    var ids = repository.findIdsByNaturalIds(naturalIds);
    if (ids.size() != naturalIds.size()) {
      ids.addAll(searchAndSaveAuthoritiesIds(naturalIds));
    }

    var authorityFetchRequest = sourceStorageClient.buildBatchFetchRequestForAuthority(ids,
      linkingRulesService.getMinAuthorityField(),
      linkingRulesService.getMaxAuthorityField());

    return sourceStorageClient.fetchParsedRecordsInBatch(authorityFetchRequest);
  }

  private Set<UUID> searchAndSaveAuthoritiesIds(Set<String> naturalIds) {
    var query = searchClient.buildNaturalIdsQuery(naturalIds);

    var authorityData = searchClient.searchAuthorities(query, false)
      .getAuthorities().stream()
      .map(dataMapper::convertToData)
      .toList();

    return repository.saveAll(authorityData).stream()
      .map(AuthorityData::getId)
      .collect(Collectors.toSet());
  }

  private Set<String> extractNaturalIdsOfLinkableFields(List<ParsedRecordContent> records,
                                                        Map<String, List<InstanceAuthorityLinkingRule>> rules) {
    return records.stream()
      .flatMap(bibRecord -> bibRecord.getFields().entrySet().stream())
      .filter(field -> nonNull(rules.get(field.getKey())))
      .map(field -> field.getValue().getLinkDetails().getNaturalId())
      .collect(Collectors.toSet());
  }

  private Map<String, List<InstanceAuthorityLinkingRule>> rulesToBibFieldMap(List<InstanceAuthorityLinkingRule> rules) {
    return rules.stream().collect(groupingBy(InstanceAuthorityLinkingRule::getBibField));
  }
}
