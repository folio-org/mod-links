package org.folio.entlinks.exception;

import static java.lang.String.format;

import java.util.Set;
import org.folio.entlinks.exception.type.ErrorCode;

public class DeletedLinkingAuthorityException extends BaseException {

  private static final String ERROR_MESSAGE = "Cannot save links to deleted authorities: [%s]";

  public DeletedLinkingAuthorityException(Set<String> deletedAuthorityIds) {
    super(formatErrorMessage(deletedAuthorityIds), ErrorCode.VALIDATION_ERROR);
  }

  private static String formatErrorMessage(Set<String> deletedAuthorityIds) {
    return format(ERROR_MESSAGE, String.join(",\n", deletedAuthorityIds));
  }
}
