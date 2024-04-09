package org.folio.entlinks.controller;

import static org.folio.entlinks.controller.ConsortiumLinksSuggestionsIT.COLLEGE_TENANT_ID;
import static org.folio.entlinks.controller.ConsortiumLinksSuggestionsIT.UNIVERSITY_TENANT_ID;
import static org.folio.support.DatabaseHelper.AUTHORITY_ARCHIVE_TABLE;
import static org.folio.support.DatabaseHelper.AUTHORITY_SOURCE_FILE_CODE_TABLE;
import static org.folio.support.DatabaseHelper.AUTHORITY_SOURCE_FILE_TABLE;
import static org.folio.support.DatabaseHelper.AUTHORITY_TABLE;
import static org.folio.support.base.TestConstants.CENTRAL_TENANT_ID;
import static org.folio.support.base.TestConstants.authorityEndpoint;
import static org.folio.support.base.TestConstants.authoritySourceFilesEndpoint;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.exception.AuthorityArchiveConstraintException;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.TestDataUtils;
import org.folio.support.base.IntegrationTestBase;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultMatcher;

@IntegrationTest
@DatabaseCleanup(
  tables = {
    AUTHORITY_TABLE,
    AUTHORITY_ARCHIVE_TABLE,
    AUTHORITY_SOURCE_FILE_CODE_TABLE,
    AUTHORITY_SOURCE_FILE_TABLE},
  tenants = {CENTRAL_TENANT_ID, COLLEGE_TENANT_ID, UNIVERSITY_TENANT_ID})
class ConsortiumAuthoritySourceFilesIT extends IntegrationTestBase {

  public static final String COLLEGE_TENANT_ID = "college";
  public static final String UNIVERSITY_TENANT_ID = "university";

  private static final String BASE_URL = "id.loc.gov/authorities/names/";
  private static final String FULL_BASE_URL = "http://" + BASE_URL;
  private static final String AUTHORITY_ID = "417f3355-081c-4aae-9209-ccb305f25f7e";
  private static final String NATURAL_ID = "n12345";

  @BeforeAll
  static void prepare() {
    setUpConsortium(CENTRAL_TENANT_ID, List.of(COLLEGE_TENANT_ID, UNIVERSITY_TENANT_ID), true);
  }

  @Test
  @SneakyThrows
  @DisplayName("DELETE: Should not delete authority source file with referenced authority archive in member tenant")
  void deleteAsfWithAuthorityArchiveReference_negative_failDeletingAsf() {
    var sourceFile = TestDataUtils.AuthorityTestData.authoritySourceFile(0);
    sourceFile.setBaseUrl(BASE_URL);
    sourceFile.setBaseUrlProtocol("http");

    databaseHelper.saveAuthoritySourceFile(CENTRAL_TENANT_ID, sourceFile);
    databaseHelper.saveAuthoritySourceFile(COLLEGE_TENANT_ID, sourceFile);
    databaseHelper.saveAuthoritySourceFile(UNIVERSITY_TENANT_ID, sourceFile);

    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, COLLEGE_TENANT_ID));
    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, UNIVERSITY_TENANT_ID));
    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, CENTRAL_TENANT_ID));

    var authorityDto = new AuthorityDto()
      .id(UUID.fromString(AUTHORITY_ID))
      .sourceFileId(sourceFile.getId())
      .naturalId(NATURAL_ID)
      .source("MARC")
      .personalName("Personal Name")
      .subjectHeadings("a");

    doPost(authorityEndpoint(), authorityDto, tenantHeaders(COLLEGE_TENANT_ID));
    awaitUntilAsserted(() ->
      assertEquals(1, databaseHelper.countRows(AUTHORITY_TABLE, COLLEGE_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_TABLE, UNIVERSITY_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_TABLE, CENTRAL_TENANT_ID)));

    doDelete(authorityEndpoint(authorityDto.getId()), tenantHeaders(COLLEGE_TENANT_ID));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_TABLE, COLLEGE_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(1, databaseHelper.countRows(AUTHORITY_ARCHIVE_TABLE, COLLEGE_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_TABLE, UNIVERSITY_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_ARCHIVE_TABLE, UNIVERSITY_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_TABLE, CENTRAL_TENANT_ID)));
    awaitUntilAsserted(() ->
      assertEquals(0, databaseHelper.countRows(AUTHORITY_ARCHIVE_TABLE, CENTRAL_TENANT_ID)));

    tryDelete(authoritySourceFilesEndpoint(sourceFile.getId()), tenantHeaders(CENTRAL_TENANT_ID))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(exceptionMatch(AuthorityArchiveConstraintException.class))
      .andExpect(errorMessageMatch(is("Cannot complete operation on the entity due to it's relation with"
        + " Authority Archive/Authority.")));

    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, COLLEGE_TENANT_ID));
    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, UNIVERSITY_TENANT_ID));
    assertEquals(2, databaseHelper.countRows(AUTHORITY_SOURCE_FILE_TABLE, CENTRAL_TENANT_ID));
  }

  private ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.errors.[0].message", errorMessageMatcher);
  }

  private HttpHeaders tenantHeaders(String tenant) {
    var httpHeaders = defaultHeaders();
    httpHeaders.put(XOkapiHeaders.TENANT, Collections.singletonList(tenant));
    return httpHeaders;
  }
}
