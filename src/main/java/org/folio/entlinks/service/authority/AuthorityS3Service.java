package org.folio.entlinks.service.authority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.s3.client.FolioS3Client;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorityS3Service {

  private final FolioS3Client s3Client;
  private final ObjectMapper objectMapper;

  public Stream<AuthorityDto> readStream(String remoteFileName) {
    var inputStream = s3Client.read(remoteFileName);
    return new BufferedReader(new InputStreamReader(inputStream))
      .lines()
      .map(s -> {
        try {
          return objectMapper.readValue(s, AuthorityDto.class);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
  }

  public void writeErrors(String initialFileName, String errorEntitiesFileName, String errorsFileName)
    throws IOException {
    var inititalPath = Paths.get(initialFileName);
    var fileLocation = "";
    if (inititalPath.getParent() != null) {
      fileLocation = inititalPath.getParent().toString();
    }
    var errorEntitiesPath = Paths.get(errorEntitiesFileName);
    var errorsPath = Paths.get(errorsFileName);

    s3Client.write(fileLocation + "/" + errorEntitiesPath.getFileName(), Files.newInputStream(errorEntitiesPath));
    s3Client.write(fileLocation + "/" + errorsPath.getFileName(), Files.newInputStream(errorsPath));
  }
}
