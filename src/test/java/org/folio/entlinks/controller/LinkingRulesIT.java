package org.folio.entlinks.controller;

import lombok.SneakyThrows;
import org.folio.entlinks.model.type.ErrorCode;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.folio.support.TestUtils.convertFile;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;

@IntegrationTest
class LinkingRulesIT extends IntegrationTestBase {

  private static final String LINKING_RULES_ENDPOINT = "/links/linking-rules";
  private static final String AUTHORITY_RULES_PATH = "classpath:rules/authority-linking-rules.json";

  @Test
  @SneakyThrows
  void getLinkingRules_positive_defaultRecordType() {
    var defaultRules = convertFile(getFile(AUTHORITY_RULES_PATH));
    doGet(LINKING_RULES_ENDPOINT)
        .andExpect(content().json(defaultRules));
  }

  @Test
  @SneakyThrows
  void getLinkingRules_positive_getAuthorityRules() {
    var authorityRules = convertFile(getFile(AUTHORITY_RULES_PATH));
    doGet(LINKING_RULES_ENDPOINT, Map.of("recordType", "AUTHORITY"))
        .andExpect(content().json(authorityRules));
  }

  @Test
  @SneakyThrows
  void getLinkingRules_negative_invalidRecordType() {
    tryGet(LINKING_RULES_ENDPOINT, Map.of("recordType", "INVALID"))
        .andExpect(status().isBadRequest())
        .andExpect(errorTotalMatch(1))
        .andExpect(errorCodeMatch(is(ErrorCode.VALIDATION_ERROR.getValue())))
        .andExpect(errorTypeMatch(is("MethodArgumentTypeMismatchException")))
        .andExpect(errorMessageMatch(containsString("No enum constant")));
  }
}
