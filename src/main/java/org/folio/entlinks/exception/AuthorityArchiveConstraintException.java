package org.folio.entlinks.exception;

import static org.folio.entlinks.config.constants.ErrorCode.VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_ARCHIVE_AND_SOURCE_FILE;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;

public class AuthorityArchiveConstraintException extends RuntimeException {

  private static final String MESSAGE = "Authority source file has Authority Archive reference";
  private static final String CVE_MESSAGE = VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_ARCHIVE_AND_SOURCE_FILE.getMessage();
  private static final String CVE_CONSTRAINT = "authority_archive_source_file_id_foreign_key";

  public AuthorityArchiveConstraintException() {
    super(MESSAGE, new ConstraintViolationException(CVE_MESSAGE, new SQLException(), CVE_CONSTRAINT));
  }
}
