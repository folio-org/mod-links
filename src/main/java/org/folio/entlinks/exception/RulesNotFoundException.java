package org.folio.entlinks.exception;

import org.folio.entlinks.model.type.ErrorCode;
import org.folio.qm.domain.dto.RecordType;

public class RulesNotFoundException extends BaseException {

  private static final String MESSAGE = "Failed to read rules for \"%s\" record type";

  public RulesNotFoundException(RecordType recordType) {
    super(String.format(MESSAGE, recordType.getValue()), ErrorCode.NOT_FOUND_ERROR);
  }
}
