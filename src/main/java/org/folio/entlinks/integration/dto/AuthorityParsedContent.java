package org.folio.entlinks.integration.dto;

import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AuthorityParsedContent extends SourceParsedContent {
  private final String naturalId;
  private final UUID sourceFileId;

  public AuthorityParsedContent(UUID id, String naturalId, String leader,
                                List<FieldParsedContent> fields, UUID sourceFileId) {
    super(id, leader, fields);
    this.naturalId = naturalId;
    this.sourceFileId = sourceFileId;
  }
}
