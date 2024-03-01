package org.folio.entlinks.config.constants;

import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_ID;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_SOURCE_FILE_CODE;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_SOURCE_FILE_ID;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_SOURCE_FILE_NAME;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_SOURCE_FILE_SEQUENCE;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_AUTHORITY_SOURCE_FILE_URL;
import static org.folio.entlinks.config.constants.ErrorCode.DUPLICATE_NOTE_TYPE_NAME;
import static org.folio.entlinks.config.constants.ErrorCode.UNKNOWN_CONSTRAIN;
import static org.folio.entlinks.config.constants.ErrorCode.VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_AND_SOURCE_FILE;
import static org.folio.entlinks.config.constants.ErrorCode.VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_ARCHIVE_AND_SOURCE_FILE;

import java.util.Map;

public class Constrains {

  private static final Map<String, ErrorCode> DB_CONSTRAINS_I18N_MAP = Map.of(
    "authority_note_type_name_unq", DUPLICATE_NOTE_TYPE_NAME,
    "authority_source_file_name_unq", DUPLICATE_AUTHORITY_SOURCE_FILE_NAME,
    "authority_source_file_base_url_unq", DUPLICATE_AUTHORITY_SOURCE_FILE_URL,
    "authority_source_file_code_unq", DUPLICATE_AUTHORITY_SOURCE_FILE_CODE,
    "pk_authority_storage", DUPLICATE_AUTHORITY_ID,
    "authority_storage_source_file_id_foreign_key", VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_AND_SOURCE_FILE,
    "authority_archive_source_file_id_foreign_key", VIOLATION_OF_RELATION_BETWEEN_AUTHORITY_ARCHIVE_AND_SOURCE_FILE,
    "authority_source_file_sequence_name_unq", DUPLICATE_AUTHORITY_SOURCE_FILE_SEQUENCE,
    "pk_authority_source_file", DUPLICATE_AUTHORITY_SOURCE_FILE_ID
  );

  public static ErrorCode getErrorCode(String constrainName) {
    return DB_CONSTRAINS_I18N_MAP.getOrDefault(constrainName, UNKNOWN_CONSTRAIN);
  }
}
