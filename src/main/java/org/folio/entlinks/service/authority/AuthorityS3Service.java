package org.folio.entlinks.service.authority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.controller.converter.AuthorityMapper;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.utils.EntitiesErrorFileWriter;
import org.folio.s3.client.FolioS3Client;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorityS3Service {

  private final FolioS3Client s3Client;
  private final ObjectMapper objectMapper;
  private final AuthorityMapper mapper;

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

  public int processAuthorities(AuthoritiesBulkContext bulkContext,
                                Consumer<List<Authority>> bulkConsumer) {
    int errorCounter = 0;
    var authorities = readStream(bulkContext.getInitialFilePath())
      .map(mapper::toEntity)
      .toList();

    try {
      bulkConsumer.accept(authorities);
    } catch (Exception e) {
      log.error("Batch failed: ", e);

      try (var writer = new EntitiesErrorFileWriter(bulkContext.getLocalFailedEntitiesFile(),
        bulkContext.getLocalErrorsFile(), objectMapper)) {
        for (Authority authority : authorities) {
          try {
            bulkConsumer.accept(List.of(authority));
          } catch (Exception ex) {
            errorCounter++;
            writer.write(authority, ex, a -> a.getId().toString());
          }
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    if (errorCounter != 0) {
      s3Client.upload(bulkContext.getLocalFailedEntitiesFilePath(), bulkContext.getFailedEntitiesFilePath());
      s3Client.upload(bulkContext.getLocalErrorsFilePath(), bulkContext.getErrorsFilePath());
    }
    bulkContext.deleteLocalFiles();
    return errorCounter;
  }
}
