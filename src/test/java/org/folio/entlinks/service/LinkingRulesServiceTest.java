package org.folio.entlinks.service;

import org.folio.entlinks.exception.RulesNotFoundException;
import org.folio.entlinks.model.entity.LinkingRules;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.RecordType;
import org.folio.support.types.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkingRulesServiceTest {

  private static final String TEST_RULE_CONTENT = "\"test-rule\"\n";

  @Mock
  private RecordType recordType;

  @Mock
  private LinkingRulesRepository repository;

  private LinkingRulesService service;


  @BeforeEach
  void setUp() {
    service = new LinkingRulesService(repository);
  }

  @Test
  void saveDefaultRules_positive_getByRecordType() {
    when(recordType.getValue()).thenReturn("TEST");

    var expectedSavedRule = LinkingRules.builder()
        .recordType(recordType.getValue())
        .rules(TEST_RULE_CONTENT)
        .build();

    service.saveDefaultRules(recordType);

    verify(repository).save(expectedSavedRule);
  }

  @Test
  void saveDefaultRules_negative_notFoundRulesFile() {
    when(recordType.getValue()).thenReturn("INVALID");

    var exception = Assertions.assertThrows(RulesNotFoundException.class,
        () -> service.saveDefaultRules(recordType));

    assertThat(exception)
        .hasMessage("Failed to read rules by \"rules/INVALID-linking-rules.json\" path");
  }
}
