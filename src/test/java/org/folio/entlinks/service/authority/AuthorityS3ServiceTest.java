package org.folio.entlinks.service.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.folio.entlinks.controller.converter.AuthorityMapperImpl;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.s3.client.FolioS3Client;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthorityS3ServiceTest {

  private static final String AUTHORITY_UUID = "58949d4b-2da2-43ce-b12b-319dd22f5990";
  @Mock
  private FolioS3Client s3Client;
  @Mock
  private AuthorityMapperImpl mapper;
  @Mock
  private Consumer<List<Authority>> bulkConsumer;
  private AuthorityS3Service authorityS3Service;

  @BeforeEach
  void setUp() {
    authorityS3Service = new AuthorityS3Service(s3Client, new ObjectMapper(), mapper);
  }

  @Test
  void readFile_ReturnsListOfStringAuthority() {
    // Arrange
    var remoteFileName = "test-file";
    var authorityJson = "{\"id\": \"" + AUTHORITY_UUID + "\", \"personalName\": \"Test Authority\"}";
    var inputStream = new ByteArrayInputStream(authorityJson.getBytes());
    when(s3Client.read(remoteFileName)).thenReturn(inputStream);

    // Act
    var resultList = authorityS3Service.readFile(remoteFileName);

    // Assert
    assertEquals(1, resultList.size());
    var stringAuthority = resultList.get(0);
    assertThat(stringAuthority).contains(AUTHORITY_UUID, "Test Authority");
  }

  @Test
  void processAuthorities_SuccessfulProcessing_NoErrorReturned() throws IOException {
    // Arrange
    AuthoritiesBulkContext bulkContext = mock(AuthoritiesBulkContext.class);
    String authorityJson = "{\"id\": \"" + AUTHORITY_UUID + "\", \"personalName\": \"Test Authority\"}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(authorityJson.getBytes());
    when(s3Client.read(any())).thenReturn(inputStream);
    when(mapper.toEntity(any())).thenCallRealMethod();

    // Act
    int errorCount = authorityS3Service.processAuthorities(bulkContext, bulkConsumer);

    // Assert
    assertEquals(0, errorCount);
    var testAuthority = new Authority();
    testAuthority.setId(UUID.fromString(AUTHORITY_UUID));
    testAuthority.setHeading("Test Authority");
    testAuthority.setHeadingType("personalName");
    verify(bulkConsumer).accept(List.of(testAuthority));
    verify(bulkContext).deleteLocalFiles();
    verify(s3Client, never()).upload(any(), any());
  }

  @Test
  void processAuthorities_multipleAuthorities_invalidId() throws IOException {
    // Arrange
    var bulkContext = spy(new AuthoritiesBulkContext("test"));
    var authoritiesJson = "{\"id\": \"" + AUTHORITY_UUID + "\", \"personalName\": \"Test Authority 1\"}\n"
      + "{\"id\": \"invalidId\", \"personalName\": \"Test Authority 2\"}";
    var inputStream = new ByteArrayInputStream(authoritiesJson.getBytes());
    when(s3Client.read(any())).thenReturn(inputStream);
    when(mapper.toEntity(any())).thenCallRealMethod();

    // Act
    int errorCount = authorityS3Service.processAuthorities(bulkContext, bulkConsumer);

    // Assert
    assertEquals(1, errorCount);
    var testAuthority = new Authority();
    testAuthority.setId(UUID.fromString(AUTHORITY_UUID));
    testAuthority.setHeading("Test Authority 1");
    testAuthority.setHeadingType("personalName");
    verify(bulkConsumer).accept(List.of(testAuthority));
    verifyNoMoreInteractions(bulkConsumer);
    verify(bulkContext).deleteLocalFiles();
    verify(s3Client, times(2)).upload(any(), any());
  }

}
