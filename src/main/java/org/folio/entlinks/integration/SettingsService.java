package org.folio.entlinks.integration;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.entlinks.client.SettingsClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SettingsService {

  private static final int DEFAULT_REQUEST_LIMIT = 10000;
  private static final String AUTHORITIES_EXPIRE_SETTING_SCOPE = "mod-entities-links";
  private static final String AUTHORITIES_EXPIRE_SETTING_KEY = "authority-archives-retention";

  private final SettingsClient settingsClient;

  public Optional<SettingsClient.SettingEntry> getAuthorityExpireSetting() {
    var settingsEntries = settingsClient.getSettingsEntries(DEFAULT_REQUEST_LIMIT);

    if (settingsEntries == null || CollectionUtils.isEmpty(settingsEntries.items())) {
      return Optional.empty();
    }

    return settingsEntries.items().stream()
        .filter(entry -> entry.scope().equals(AUTHORITIES_EXPIRE_SETTING_SCOPE))
        .filter(entry -> entry.key().equals(AUTHORITIES_EXPIRE_SETTING_KEY))
        .findFirst();
  }
}
