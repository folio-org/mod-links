package org.folio.entlinks.utils;

import static java.util.Objects.nonNull;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.folio.entlinks.domain.entity.AuthoritySourceFile;

@UtilityClass
public class FieldUtils {

  /**
   * Extract naturalId from subfield $0.
   *
   * @param zeroValue subfield $0 value as {@link String}
   * @return trimmed naturalId as {@link String}
   */
  public static String trimSubfield0Value(String zeroValue) {
    if (nonNull(zeroValue)) {
      var slashIndex = zeroValue.lastIndexOf('/');
      if (slashIndex != -1) {
        return zeroValue.substring(slashIndex + 1);
      }
    }
    return zeroValue;
  }

  /**
   * Returns subfield $0 value with baseUrl from sourceFile.
   *
   * @param naturalId  Authority natural id as {@link String}
   * @param sourceFile Authority source file as {@link AuthoritySourceFile}
   * @return subfield $0 value as {@link String}
   */
  public static String getSubfield0Value(String naturalId, AuthoritySourceFile sourceFile) {
    if (nonNull(naturalId) && nonNull(sourceFile) && nonNull(sourceFile.getBaseUrl()) && isValidPrefix(naturalId)) {
      return StringUtils.appendIfMissing(sourceFile.getFullBaseUrl(), "/") + naturalId;
    } else {
      return naturalId;
    }
  }

  private boolean isValidPrefix(String naturalId) {
    String prefix = naturalId.split("[0-9]")[0];
    try {
      SubfieldPrefix.valueOf(prefix.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

}
