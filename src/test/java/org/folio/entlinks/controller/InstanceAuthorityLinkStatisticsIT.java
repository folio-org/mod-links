package org.folio.entlinks.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.entity.AuthorityDataStatAction;
import org.folio.entlinks.domain.repository.AuthorityDataStatRepository;
import org.folio.entlinks.utils.DateUtils;
import org.folio.spring.test.type.IntegrationTest;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.model.ResultList;
import org.folio.support.TestUtils;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@IntegrationTest
class InstanceAuthorityLinkStatisticsIT extends IntegrationTestBase {

  private static final String LINK_STATISTICS_ENDPOINT = "/links/authority/stats";
  private static final OffsetDateTime FROM_DATE = OffsetDateTime.of(2020, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC);
  private static final OffsetDateTime TO_DATE = OffsetDateTime.of(2021, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC);
  private static final Integer LIMIT = 2;
  private static final AuthorityDataStatActionDto STAT_ACTION_DTO = AuthorityDataStatActionDto.UPDATE_HEADING;
  private @MockBean AuthorityDataStatRepository authorityDataStatRepository;

  @Test
  @SneakyThrows
  void getAuthDataStat_positive_whenStatsIsEmpty() {
    var preparedLink = LINK_STATISTICS_ENDPOINT + "?action=" + STAT_ACTION_DTO
      + "&fromDate=" + FROM_DATE
      + "&toDate=" + TO_DATE + "&limit=" + LIMIT;
    doGet(preparedLink)
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getAuthDataStat_positive_whenStatsIsNotEmpty() throws Exception {
    UUID userId1 = randomUUID();
    UUID userId2 = randomUUID();
    var list = TestUtils.dataStatList(userId1, userId2, AuthorityDataStatAction.UPDATE_HEADING);
    ResultList<UsersClient.User> userResultList = TestUtils.usersList(List.of(userId1, userId2));
    okapi.wireMockServer().stubFor(get(urlPathEqualTo("/users"))
      .withQueryParam("query", equalTo("id==" + userId1 + " or id==" + userId2))
      .willReturn(aResponse().withBody(OBJECT_MAPPER.writeValueAsString(userResultList))
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withStatus(HttpStatus.SC_OK)));
    when(authorityDataStatRepository.findByActionAndStartedAtGreaterThanEqualAndStartedAtLessThanEqual(
      eq(AuthorityDataStatAction.valueOf(STAT_ACTION_DTO.getValue())),
      eq(DateUtils.toTimestamp(FROM_DATE)),
      eq(DateUtils.toTimestamp(TO_DATE)),
      any()
    )).thenReturn(list);

    var preparedLink = LINK_STATISTICS_ENDPOINT + "?action=" + STAT_ACTION_DTO
      + "&fromDate=" + FROM_DATE
      + "&toDate=" + TO_DATE + "&limit=" + LIMIT;

    var authorityDataStat = list.get(0);
    UsersClient.User.Personal personal = userResultList.getResult().get(0).personal();
    String startedAtStr = DateUtils.fromTimestamp(authorityDataStat.getStartedAt()).toString();
    String completedAtStr = DateUtils.fromTimestamp(authorityDataStat.getCompletedAt()).toString();
    doGet(preparedLink)
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$.stats[0].action", is(AuthorityDataStatActionDto.UPDATE_HEADING.name())))
      .andExpect(jsonPath("$.stats[0].metadata.startedByUserFirstName", is(personal.firstName())))
      .andExpect(jsonPath("$.stats[0].metadata.startedByUserLastName", is(personal.lastName())))
      .andExpect(jsonPath("$.stats[0].metadata.startedAt", is(startedAtStr)))
      .andExpect(jsonPath("$.stats[0].metadata.completedAt", is(completedAtStr)))
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
}
