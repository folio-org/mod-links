package org.folio.entlinks.exception;

import org.folio.entlinks.model.type.ErrorCode;

public class RulesNotFoundException extends BaseException {

  private static final String MESSAGE = "Failed to read rules by \"%s\" path";

  public RulesNotFoundException(String path) {
    super(String.format(MESSAGE, path), ErrorCode.NOT_FOUND_ERROR);
  }
}
