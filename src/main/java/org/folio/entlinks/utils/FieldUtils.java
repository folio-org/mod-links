package org.folio.entlinks.utils;

import static java.util.Objects.nonNull;

import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.folio.entlinks.client.AuthoritySourceFileClient.AuthoritySourceFile;

@UtilityClass
public class FieldUtils {

  public static String trimZeroValue(String zeroValue) {
    if (nonNull(zeroValue)) {
      var slashIndex = zeroValue.lastIndexOf('/');
      if (slashIndex != -1) {
        return zeroValue.substring(slashIndex + 1);
      }
    }
    return zeroValue;
  }

  public static String getSubfield0Value(String naturalId, AuthoritySourceFile sourceFile) {
    var subfield0Value = "";
    if (nonNull(naturalId) && nonNull(sourceFile)) {
      subfield0Value = StringUtils.appendIfMissing(sourceFile.baseUrl(), "/");
    }
    return subfield0Value + naturalId;
  }

  public static String getSubfield0Value(Map<UUID, AuthoritySourceFile> files, String naturalId) {
    var sourceFile = files.values().stream()
      .filter(file -> file.codes().stream().anyMatch(naturalId::startsWith))
      .findFirst()
      .orElse(null);

    if (nonNull(sourceFile)) {
      return getSubfield0Value(naturalId, sourceFile);
    }
    return naturalId;
  }
}
