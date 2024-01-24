package org.folio.entlinks.controller;

import static org.folio.support.DatabaseHelper.AUTHORITY_TABLE;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.folio.support.base.TestConstants.authorityEndpoint;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.folio.entlinks.domain.dto.AuthorityBulkCreateRequest;
import org.folio.s3.client.FolioS3Client;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.base.IntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

@IntegrationTest
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
    var dto = new AuthorityBulkCreateRequest(filename);

    tryPost(authorityEndpoint() + "/bulk", dto)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.errorsNumber", is(1)));

    assumeTrue(databaseHelper.countRows(AUTHORITY_TABLE, TENANT_ID) == 1);
    var list = s3Client.list("parentLocation/filePath/");
    assertEquals(3, list.size());
  }

}
