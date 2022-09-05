package org.folio.entlinks.exception;

import lombok.Getter;
import org.folio.entlinks.model.type.ErrorCode;

/**
 * Base exception class that is used for all exceptional situations
 */
@Getter
public abstract class BaseException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * Initialize exception with provided message and error code.
   *
   * @param message   exception message
   * @param errorCode exception code {@link ErrorCode}
   */
  protected BaseException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Initialize exception with provided message, cause exception and error code.
   *
   * @param message   exception message
   * @param cause     exception cause {@link Throwable}
   * @param errorCode exception code {@link ErrorCode}
   */
  protected BaseException(String message, Throwable cause, ErrorCode errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
