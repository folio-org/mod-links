package org.folio.entlinks.client;

import java.util.UUID;
import org.folio.entlinks.domain.dto.FetchParsedRecordsBatchRequest;
import org.folio.entlinks.domain.dto.SourceRecord;
import org.folio.entlinks.domain.dto.StrippedParsedRecordCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("source-storage")
public interface SourceStorageClient {

  @GetMapping("/source-records/{id}?idType=AUTHORITY")
  SourceRecord getMarcAuthorityById(@PathVariable("id") UUID id);

  @PostMapping("/batch/parsed-records/fetch")
  StrippedParsedRecordCollection fetchParsedRecordsInBatch(FetchParsedRecordsBatchRequest recordsBatchRequest);
}
