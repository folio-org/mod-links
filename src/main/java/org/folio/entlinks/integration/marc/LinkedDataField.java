package org.folio.entlinks.integration.marc;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.marc4j.marc.impl.DataFieldImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class LinkedDataField extends DataFieldImpl {
  private UUID authorityId;
  private String naturalId;
  private String linkStatus;
  private int ruleId;

  LinkedDataField() {
    super("", ' ', ' ');
  }
}
