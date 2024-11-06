package org.folio.entlinks.integration.dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

  public Optional<FieldParsedContent> getFieldByTag(String fieldTag) {
    if (StringUtils.isEmpty(fieldTag)) {
      return Optional.empty();
    }
    return getFields().stream()
      .filter(fieldParsedContent -> fieldParsedContent.getTag().equals(fieldTag))
      .findFirst();
  }
}
