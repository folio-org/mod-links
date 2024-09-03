package org.folio.entlinks.service.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.folio.s3.client.FolioS3Client;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BulkAuthorityS3ClientTest {

  private static final String AUTHORITY_UUID = "58949d4b-2da2-43ce-b12b-319dd22f5990";
  @Mock
  private FolioS3Client s3Client;
  @InjectMocks
  private BulkAuthorityS3Client client;

  @Test
  void readFile_ReturnsListOfStringAuthority() {
    // Arrange
    var remoteFileName = "test-file";
    var authorityJson = "{\"id\": \"" + AUTHORITY_UUID + "\", \"personalName\": \"Test Authority\"}";
    var inputStream = new ByteArrayInputStream(authorityJson.getBytes());
    when(s3Client.read(remoteFileName)).thenReturn(inputStream);

    // Act
    var resultList = client.readFile(remoteFileName);

    // Assert
    assertEquals(1, resultList.size());
    var stringAuthority = resultList.get(0);
    assertThat(stringAuthority).contains(AUTHORITY_UUID, "Test Authority");
  }

  @Test
  void uploadErrorFiles_uploads() throws IOException {
    // Arrange
    var remoteFileName = "test-file";
    var bulkContext = new AuthoritiesBulkContext(remoteFileName);

    // Act
    client.uploadErrorFiles(bulkContext);

    // Assert
    verify(s3Client).upload(bulkContext.getLocalFailedEntitiesFilePath(), bulkContext.getFailedEntitiesFilePath());
    verify(s3Client).upload(bulkContext.getLocalErrorsFilePath(), bulkContext.getErrorsFilePath());
  }

}
