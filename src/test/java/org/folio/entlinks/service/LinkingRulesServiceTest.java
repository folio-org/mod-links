package org.folio.entlinks.service;

import lombok.SneakyThrows;
import org.folio.entlinks.exception.RulesNotFoundException;
import org.folio.entlinks.model.entity.LinkingRules;
import org.folio.entlinks.repository.LinkingRulesRepository;
import org.folio.qm.domain.dto.RecordType;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.domain.dto.RecordType.AUTHORITY;
import static org.folio.support.TestUtils.convertFile;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkingRulesServiceTest {

  private static final String AUTHORITY_RULES_PATH = "classpath:rules/authority-linking-rules.json";

  @Mock
  private LinkingRulesRepository repository;

  private LinkingRulesService service;


  @BeforeEach
  void setUp() {
    service = new LinkingRulesService(repository);
  }

  @Test
  @SneakyThrows
  void saveDefaultRules_positive_saveByRecordType() {
    var expectedFile = getFile(AUTHORITY_RULES_PATH);
    var expectedSavedRule = LinkingRules.builder()
        .recordType(AUTHORITY.getValue())
        .rules(convertFile(expectedFile))
        .build();

    try (var mockedFiles = mockStatic(ResourceUtils.class)) {
      mockedFiles.when(() -> getFile(anyString())).thenReturn(expectedFile);
      service.saveDefaultRules(AUTHORITY);
    }

    verify(repository).save(expectedSavedRule);
  }

  @Test
  void saveDefaultRules_negative_notFoundRulesFile() {
    var mockedRecordType = mock(RecordType.class);

    when(mockedRecordType.getValue()).thenReturn("INVALID");

    var exception = Assertions.assertThrows(RulesNotFoundException.class,
        () -> service.saveDefaultRules(mockedRecordType));

    assertThat(exception)
        .hasMessage("Failed to read rules for \"INVALID\" record type");
  }
}
