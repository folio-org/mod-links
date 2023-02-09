package org.folio.entlinks.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.SneakyThrows;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

@IntegrationTest
class InstanceAuthorityLinkStatisticsIT extends IntegrationTestBase {

  private static final String LINK_STATISTICS_ENDPOINT = "/links/authority/stats";
  private static final OffsetDateTime FROM_DATE = OffsetDateTime.of(2020, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC);
  private static final OffsetDateTime TO_DATE = OffsetDateTime.of(2021, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC);
  private static final Integer LIMIT = 2;
  private static final AuthorityDataStatActionDto STAT_ACTION_DTO = AuthorityDataStatActionDto.UPDATE_HEADING;

  @Test
  @SneakyThrows
  void getAuthDataStat_positive_getAuthorityDataStats() {
    var preparedLink = LINK_STATISTICS_ENDPOINT + "?action=" + STAT_ACTION_DTO
                        + "&fromDate=" + FROM_DATE
                        + "&toDate=" + TO_DATE + "&limit=" + LIMIT;
    doGet(preparedLink)
      .andExpect(status().isOk());
  }
}
