package org.folio.entlinks.utils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

@UtilityClass
public class KafkaUtils {

  public static List<Header> toKafkaHeaders(Map<String, Collection<String>> requestHeaders) {
    if (requestHeaders == null || requestHeaders.isEmpty()) {
      return Collections.emptyList();
    }
    return requestHeaders.entrySet().stream()
      .map(header -> (Header) new RecordHeader(header.getKey(),
        retrieveFirstSafe(header.getValue()).getBytes(StandardCharsets.UTF_8)))
      .toList();
  }

  private String retrieveFirstSafe(Collection<String> strings) {
    return strings != null && !strings.isEmpty() ? strings.iterator().next() : "";
  }
}
