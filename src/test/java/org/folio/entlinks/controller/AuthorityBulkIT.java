package org.folio.entlinks.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.DatabaseHelper.AUTHORITY_TABLE;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.folio.support.base.TestConstants.authorityEndpoint;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.folio.entlinks.domain.dto.AuthorityBulkRequest;
import org.folio.s3.client.FolioS3Client;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.DatabaseHelper;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

@IntegrationTest
@DatabaseCleanup(tables = {
  DatabaseHelper.AUTHORITY_SOURCE_FILE_CODE_TABLE,
  DatabaseHelper.AUTHORITY_DATA_STAT_TABLE,
  DatabaseHelper.AUTHORITY_TABLE,
  DatabaseHelper.AUTHORITY_SOURCE_FILE_TABLE})
class AuthorityBulkIT extends IntegrationTestBase {

  private @Autowired FolioS3Client s3Client;
  private @Autowired ResourceLoader loader;

  @BeforeAll
  static void prepare() throws IOException {
    setUpTenant(true);
  }

  @Test
  @DisplayName("POST: create new Authority with defined ID")
  void createAuthority_positive_entityCreatedWithProvidedId() throws Exception {
    var resource = loader.getResource("classpath:test-data/authorities/bulkAuthorities");
    var filename = s3Client.write("parentLocation/filePath/fileName", resource.getInputStream());
    var dto = new AuthorityBulkRequest(filename);

    tryPost(authorityEndpoint() + "/bulk", dto)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.errorsNumber", is(2)));

    assumeTrue(databaseHelper.countRows(AUTHORITY_TABLE, TENANT_ID) == 1);
    var list = s3Client.list("parentLocation/filePath/");
    assertThat(list)
      .hasSize(3)
      .containsExactly("parentLocation/filePath/fileName",
        "parentLocation/filePath/fileName_errors",
        "parentLocation/filePath/fileName_failedEntities");
    var errors = new BufferedReader(new InputStreamReader(s3Client.read("parentLocation/filePath/fileName_errors")))
        .lines()
        .toList(); //af045f2f-e851-4613-984c-4bc13430454a asf not present
    assertThat(errors)
      .hasSize(2)
      .anyMatch(s -> s.contains("constraint [authority_storage_source_file_id_foreign_key]"))
      .anyMatch(s -> s.contains("Unexpected json parsing exception")); //invalid UUID for noteTypeId
  }

}
