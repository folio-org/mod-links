package org.folio.entlinks.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.entlinks.config.constants.Constrains;
import org.folio.entlinks.config.constants.ErrorCode;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
public class ConstrainsTest {
  private static final String EXISTING_CONSTRAIN = "pk_authority_storage";

  @Test
  void getErrorCode_shouldReturnErrorCode() {
    var errorCode = Constrains.getErrorCode(EXISTING_CONSTRAIN);
    assertEquals(ErrorCode.DUPLICATE_AUTHORITY_ID, errorCode);
  }

  @Test
  void getErrorCode_shouldReturnDefaultErrorCode() {
    var errorCode = Constrains.getErrorCode("constrain_does_not_exist");
    assertEquals(ErrorCode.UNKNOWN_CONSTRAIN, errorCode);
  }
}
