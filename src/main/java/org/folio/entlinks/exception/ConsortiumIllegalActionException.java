package org.folio.entlinks.exception;

import org.folio.entlinks.exception.type.ErrorType;

public class ConsortiumIllegalActionException extends BaseException {

  private static final String MSG_TEMPLATE = "%s is not applicable to consortium shadow copy";

  public ConsortiumIllegalActionException(String action) {
    super(MSG_TEMPLATE.formatted(action), ErrorType.VALIDATION_ERROR);
  }

  public ConsortiumIllegalActionException(String action, Throwable cause) {
    super(MSG_TEMPLATE.formatted(action), ErrorType.VALIDATION_ERROR, cause);
  }
}
