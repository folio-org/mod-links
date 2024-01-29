package org.folio.entlinks.service.authority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.controller.converter.AuthorityMapper;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.exception.FolioIntegrationException;
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
    log.info("readStream::Reading authorities from the file [filename: {}]", remoteFileName);
    var inputStream = s3Client.read(remoteFileName);
    return new BufferedReader(new InputStreamReader(inputStream))
      .lines()
      .map(this::toAuthorityDto);
  }

  public int processAuthorities(AuthoritiesBulkContext bulkContext,
                                Consumer<List<Authority>> bulkConsumer) {
    log.info("processAuthorities::Processing bulk authority request [filename: {}]", bulkContext.getInitialFilePath());
    AtomicInteger errorCounter = new AtomicInteger();
    var authorities = readStream(bulkContext.getInitialFilePath())
      .map(mapper::toEntity)
      .toList();

    try {
      bulkConsumer.accept(authorities);
    } catch (Exception e) {
      log.error("processAuthorities::Batch failed: [message: {}]", e.getMessage());
      log.debug("processAuthorities::Batch failed", e);

      try (var writer = new EntitiesErrorFileWriter(bulkContext.getLocalFailedEntitiesFile(),
        bulkContext.getLocalErrorsFile(), objectMapper)) {
        for (Authority authority : authorities) {
          processSingleAuthority(authority, bulkConsumer, errorCounter, writer);
        }
      } catch (Exception ex) {
        log.error("processAuthorities::Processing bulk authority request failed.", ex);
      }
    }

    if (errorCounter.get() != 0) {
      s3Client.upload(bulkContext.getLocalFailedEntitiesFilePath(), bulkContext.getFailedEntitiesFilePath());
      s3Client.upload(bulkContext.getLocalErrorsFilePath(), bulkContext.getErrorsFilePath());
    }
    try {
      bulkContext.deleteLocalFiles();
    } catch (IOException e) {
      log.error("processAuthorities::Exception during temp files deletion: ", e);
    }
    return errorCounter.get();
  }

  private void processSingleAuthority(Authority authority, Consumer<List<Authority>> bulkConsumer,
                                      AtomicInteger errorCounter, EntitiesErrorFileWriter writer) throws IOException {
    try {
      bulkConsumer.accept(List.of(authority));
    } catch (Exception ex) {
      errorCounter.incrementAndGet();
      writer.write(authority, ex, a -> a.getId().toString());
    }
  }

  private AuthorityDto toAuthorityDto(String s) {
    try {
      return objectMapper.readValue(s, AuthorityDto.class);
    } catch (JsonProcessingException e) {
      throw new FolioIntegrationException("Unexpected json parsing exception", e);
    }
  }
}
