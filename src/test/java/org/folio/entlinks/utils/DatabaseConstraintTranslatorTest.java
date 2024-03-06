package org.folio.entlinks.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import org.folio.entlinks.config.constants.ErrorCode;
import org.folio.spring.testing.type.UnitTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

@UnitTest
class DatabaseConstraintTranslatorTest {
  private static final String EXISTING_CONSTRAIN = "pk_authority_storage";

  @Test
  void getErrorCode_shouldReturnErrorCode() {
    var errorCode = DatabaseConstraintTranslator.translate(getException(EXISTING_CONSTRAIN));
    assertEquals(ErrorCode.DUPLICATE_AUTHORITY_ID, errorCode);
  }

  @Test
  void getErrorCode_shouldReturnDefaultErrorCode() {
    var errorCode = DatabaseConstraintTranslator.translate(getException("constrain_does_not_exist"));
    assertEquals(ErrorCode.UNKNOWN_CONSTRAINT, errorCode);
  }

  private ConstraintViolationException getException(String constraintName) {
    SQLException root = new SQLException("root");
    return new ConstraintViolationException("message", root, constraintName);
  }
}
