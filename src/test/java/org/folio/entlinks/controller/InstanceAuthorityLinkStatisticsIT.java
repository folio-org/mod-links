package org.folio.entlinks.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

@IntegrationTest
class InstanceAuthorityLinkStatisticsIT extends IntegrationTestBase {

  private static final String LINK_STATISTICS_ENDPOINT =
    "/links/authority/stats?action=UPDATE_HEADING&fromDate=2020-07-16T12:13:36.879Z&"
      + "toDate=2022-07-16T12:13:36.879Z&limit=100";

  @Test
  @SneakyThrows
  void getLinkingRules_positive_getInstanceAuthorityRules() {
    doGet(LINK_STATISTICS_ENDPOINT)
      .andExpect(status().isOk());
  }
}
