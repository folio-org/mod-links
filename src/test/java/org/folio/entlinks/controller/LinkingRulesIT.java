package org.folio.entlinks.controller;

import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.folio.entlinks.EntityLinksApplication;
import org.folio.entlinks.model.type.ErrorCode;
import org.folio.support.base.IntegrationTestBase;
import org.folio.support.types.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class LinkingRulesIT extends IntegrationTestBase {

  private static final String LINKING_RULES_ENDPOINT = "/links/linking-rules";
  private static final String AUTHORITY_RULES_PATH = "/rules/authority-linking-rules.json";

  @Test
  @SneakyThrows
  void getLinkingRules_positive_defaultRecordType() {
    var defaultRules = readResourceFromPath(AUTHORITY_RULES_PATH);
    doGet(LINKING_RULES_ENDPOINT)
        .andExpect(content().json(defaultRules));
  }

  @Test
  @SneakyThrows
  void getLinkingRules_positive_getAuthorityRules() {
    var authorityRules = readResourceFromPath(AUTHORITY_RULES_PATH);
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

  @SneakyThrows
  private String readResourceFromPath(String path) {
    URL url = getResource(EntityLinksApplication.class, path);
    return Resources.toString(url, StandardCharsets.UTF_8);
  }
}
