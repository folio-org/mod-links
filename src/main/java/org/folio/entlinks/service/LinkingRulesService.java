package org.folio.entlinks.service;

import lombok.RequiredArgsConstructor;
import org.folio.entlinks.LinkingRecords;
import org.folio.entlinks.exception.RulesNotFoundException;
import org.folio.entlinks.model.converter.LinkingRulesMapper;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private static final String LINKING_RULES_PATH_PATTERN = "classpath:linking-rules/%s.json";

  private final LinkingRulesRepository repository;
  private final LinkingRulesMapper mapper;

  public List<LinkingRuleDto> getLinkingRules(LinkingRecords recordType) {
    var jsonRules = repository.findByLinkingRecords(recordType.name());
    return mapper.convert(jsonRules);
  }

  public void saveDefaultRules(LinkingRecords linkedRecords) {
    var jsonRules = readRulesFromResources(linkedRecords);
    var rules = mapper.convert(linkedRecords, jsonRules);

    repository.save(rules);
  }

  private String readRulesFromResources(LinkingRecords linkingRecords) {
    try {
      var rulePath = String.format(LINKING_RULES_PATH_PATTERN, linkingRecords.value());
      var filePath = ResourceUtils.getFile(rulePath).toPath();

      return Files.readString(filePath);
    } catch (IOException e) {
      throw new RulesNotFoundException(linkingRecords);
    }
  }
}
