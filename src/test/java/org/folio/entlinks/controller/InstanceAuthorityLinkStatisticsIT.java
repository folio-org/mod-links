package org.folio.entlinks.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.UUID.randomUUID;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.folio.entlinks.domain.entity.AuthorityDataStatAction;
import org.folio.entlinks.support.DatabaseHelper;
import org.folio.spring.test.extension.DatabaseCleanup;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.model.ResultList;
import org.folio.support.TestUtils;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@IntegrationTest
@DatabaseCleanup(tables = {DatabaseHelper.AUTHORITY_DATA_STAT, DatabaseHelper.AUTHORITY_DATA})
class InstanceAuthorityLinkStatisticsIT extends IntegrationTestBase {

  private static final String AUTH_STATS_ENDPOINT_PATTERN = "/links/authority/stats"
    + "?action=%s&fromDate=%s&toDate=%s&limit=%d";
  private static final OffsetDateTime TO_DATE = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
  private static final OffsetDateTime FROM_DATE = TO_DATE.minus(1, ChronoUnit.MONTHS);
  private static final Integer LIMIT = 2;
  private static final AuthorityDataStatActionDto STAT_ACTION_DTO = AuthorityDataStatActionDto.UPDATE_HEADING;

  @Test
  @SneakyThrows
  void getAuthDataStat_positive_whenStatsIsEmpty() {
    doGet(getStatsUri())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.stats[0]").doesNotExist());
  }

  @Test
  void getAuthDataStat_positive_whenStatsIsNotEmpty() throws Exception {
    UUID userId1 = randomUUID();
    UUID userId2 = randomUUID();
    ResultList<UsersClient.User> userResultList = TestUtils.usersList(List.of(userId1, userId2));
    okapi.wireMockServer().stubFor(get(urlPathEqualTo("/users"))
      .withQueryParam("query", equalTo("id==" + userId1 + " or id==" + userId2))
      .willReturn(aResponse().withBody(objectMapper.writeValueAsString(userResultList))
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withStatus(HttpStatus.SC_OK)));

    var authorityDataStats = List.of(
      TestUtils.authorityDataStat(userId1, AuthorityDataStatAction.UPDATE_HEADING),
      TestUtils.authorityDataStat(userId2, AuthorityDataStatAction.UPDATE_HEADING)
    );

    for (AuthorityDataStat authorityDataStat : authorityDataStats) {
      databaseHelper.saveAuthData(authorityDataStat.getAuthorityData(), TENANT_ID, false);
      databaseHelper.saveStat(authorityDataStat, TENANT_ID);
    }

    String preparedLink = getStatsUri();

    var authorityDataStat = authorityDataStats.get(0);
    UsersClient.User.Personal personal = userResultList.getResult().get(0).personal();
    doGet(preparedLink)
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0].action", is(AuthorityDataStatActionDto.UPDATE_HEADING.name())))
      .andExpect(jsonPath("$.stats[0].metadata.startedByUserFirstName", is(personal.firstName())))
      .andExpect(jsonPath("$.stats[0].metadata.startedByUserLastName", is(personal.lastName())))
      .andExpect(jsonPath("$.stats[0].id", is(authorityDataStat.getId().toString())))
      .andExpect(jsonPath("$.stats[0].authorityId", is(authorityDataStat.getAuthorityData().getId().toString())))
      .andExpect(jsonPath("$.stats[0].lbFailed", is(authorityDataStat.getLbFailed())))
      .andExpect(jsonPath("$.stats[0].lbUpdated", is(authorityDataStat.getLbUpdated())))
      .andExpect(jsonPath("$.stats[0].lbTotal", is(authorityDataStat.getLbTotal())))
      .andExpect(jsonPath("$.stats[0].headingOld", is(authorityDataStat.getHeadingOld())))
      .andExpect(jsonPath("$.stats[0].headingNew", is(authorityDataStat.getHeadingNew())))
      .andExpect(jsonPath("$.stats[0].headingTypeOld", is(authorityDataStat.getHeadingTypeOld())))
      .andExpect(jsonPath("$.stats[0].headingTypeNew", is(authorityDataStat.getHeadingTypeNew())));
  }

  @Test
  void getAuthDataStat_positive_whenAuthorityWasDeleted() throws Exception {
    UUID userId1 = randomUUID();
    UUID userId2 = randomUUID();
    var authorityDataStats = List.of(
      TestUtils.authorityDataStat(userId1, AuthorityDataStatAction.UPDATE_HEADING),
      TestUtils.authorityDataStat(userId2, AuthorityDataStatAction.UPDATE_HEADING)
    );
    for (AuthorityDataStat authorityDataStat : authorityDataStats) {
      databaseHelper.saveAuthData(authorityDataStat.getAuthorityData(), TENANT_ID, true);
      databaseHelper.saveStat(authorityDataStat, TENANT_ID);
    }

    String preparedLink = getStatsUri();

    doGet(preparedLink)
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0]").doesNotExist());
  }

  private String getStatsUri() {
    return AUTH_STATS_ENDPOINT_PATTERN.formatted(STAT_ACTION_DTO, FROM_DATE, TO_DATE, LIMIT);
  }
}
