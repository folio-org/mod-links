package org.folio.entlinks.service.messaging.authority.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AuthorityChangeFailed(UUID jobId,
                                    List<Integer> linkIds,
                                    UUID instanceId,
                                    Status status,
                                    String failCause,
                                    String tenant,
                                    OffsetDateTime ts) {
  public enum Status {
    success, fail
  }
}
