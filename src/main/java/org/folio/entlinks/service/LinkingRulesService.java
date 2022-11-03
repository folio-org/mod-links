package org.folio.entlinks.service;

import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.exception.RulesNotFoundException;
import org.folio.entlinks.model.entity.LinkingRules;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.RecordType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LinkingRulesService {

  private static final String LINKING_RULES_PATH_PATTERN = "rules/%s-linking-rules.json";
  private final LinkingRulesRepository repository;

  public String getLinkingRules(RecordType recordType) {
    var jsonRules = repository.findByRecordType(recordType.getValue());
    return jsonRules.getRules();
  }

  public void saveDefaultRules(RecordType recordTypeEnum) {
    final var recordType = recordTypeEnum.getValue();
    var rulePath = String.format(LINKING_RULES_PATH_PATTERN, recordType);

    readRulesFromResources(rulePath).ifPresent(rules ->
        repository.save(LinkingRules.builder()
            .recordType(recordType)
            .rules(rules)
            .build())
    );
  }

  private Optional<String> readRulesFromResources(String rulePath) {
    try {
      URL url = Resources.getResource(rulePath);
      return Optional.of(Resources.toString(url, StandardCharsets.UTF_8));
    } catch (IllegalArgumentException | IOException e) {
      throw new RulesNotFoundException(rulePath);
    }
  }
}
