package org.folio.entlinks.service.authority;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.s3.client.FolioS3Client;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class BulkAuthorityS3Client {

  private final FolioS3Client s3Client;

  @Retryable(
    retryFor = Exception.class,
    maxAttemptsExpression = "${folio.remote-storage.retryCount}",
    backoff = @Backoff(delayExpression = "${folio.remote-storage.retryDelayMs}"))
  public List<String> readFile(String remoteFileName) {
    log.info("readFile::Reading lines from the file [filename: {}]", remoteFileName);
    var inputStream = s3Client.read(remoteFileName);
    return new BufferedReader(new InputStreamReader(inputStream))
      .lines()
      .toList();
  }

  @Retryable(
    retryFor = Exception.class,
    maxAttemptsExpression = "${folio.remote-storage.retryCount}",
    backoff = @Backoff(delayExpression = "${folio.remote-storage.retryDelayMs}"))
  public void uploadErrorFiles(AuthoritiesBulkContext bulkContext) {
    log.info("uploadErrorFiles::Uploading [failedEntities: {}, errors: {}]",
      bulkContext.getFailedEntitiesFilePath(), bulkContext.getErrorsFilePath());
    s3Client.upload(bulkContext.getLocalFailedEntitiesFilePath(), bulkContext.getFailedEntitiesFilePath());
    s3Client.upload(bulkContext.getLocalErrorsFilePath(), bulkContext.getErrorsFilePath());
  }
}
