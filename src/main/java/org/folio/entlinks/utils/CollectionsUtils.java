package org.folio.entlinks.utils;

import java.util.Collection;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionsUtils {

  public static boolean containsIgnoreCase(Collection<String> collection, String value) {
    return collection.stream().filter(Objects::nonNull).anyMatch(s -> s.equalsIgnoreCase(value));
  }
}
