package org.folio.entlinks.client;

import java.util.UUID;
import org.folio.qm.domain.dto.SourceRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("source-storage")
public interface SourceStorageClient {

  @GetMapping("/source-records/{id}")
  SourceRecord getMarcAuthorityById(@PathVariable("id") UUID id);
}
