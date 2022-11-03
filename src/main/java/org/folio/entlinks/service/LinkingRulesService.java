package org.folio.entlinks.service;

import lombok.RequiredArgsConstructor;
import org.folio.entlinks.exception.RulesNotFoundException;
import org.folio.entlinks.model.entity.LinkingRules;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.RecordType;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private static final String LINKING_RULES_PATH_PATTERN = "classpath:rules/%s-linking-rules.json";
  private final LinkingRulesRepository repository;

  public String getLinkingRules(RecordType recordType) {
    var jsonRules = repository.findByRecordType(recordType.getValue());
    return jsonRules.getRules();
  }

  public void saveDefaultRules(RecordType recordType) {
    var rules = readRulesFromResources(recordType);

    repository.save(LinkingRules.builder()
        .recordType(recordType.getValue())
        .rules(rules)
        .build());
  }

  private String readRulesFromResources(RecordType recordType) {
    var rulePath = String.format(LINKING_RULES_PATH_PATTERN, recordType);
    try {
      var file = ResourceUtils.getFile(rulePath);
      return new String(Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      throw new RulesNotFoundException(recordType);
    }
  }
}
