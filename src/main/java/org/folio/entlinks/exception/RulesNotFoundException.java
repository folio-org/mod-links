package org.folio.entlinks.exception;

import org.folio.entlinks.LinkingRecords;
import org.folio.entlinks.model.type.ErrorCode;

public class RulesNotFoundException extends BaseException {

  private static final String MESSAGE = "Failed to read rules for \"%s\" linking records";

  public RulesNotFoundException(LinkingRecords recordType) {
    super(String.format(MESSAGE, recordType.value()), ErrorCode.NOT_FOUND_ERROR);
  }
}
