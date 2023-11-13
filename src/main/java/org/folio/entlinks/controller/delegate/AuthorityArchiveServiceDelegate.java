package org.folio.entlinks.controller.delegate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.SettingsClient;
import org.folio.entlinks.config.properties.AuthorityArchiveProperties;
import org.folio.entlinks.domain.entity.AuthorityArchive;
import org.folio.entlinks.domain.repository.AuthorityArchiveRepository;
import org.folio.entlinks.integration.SettingsService;
import org.folio.entlinks.service.authority.AuthorityArchiveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorityArchiveServiceDelegate {

  private final AuthorityArchiveService authorityArchiveService;
  private final SettingsService settingsService;
  private final AuthorityArchiveRepository authorityArchiveRepository;
  private final AuthorityArchiveProperties authorityArchiveProperties;

  @Transactional(readOnly = true)
  public void expire() {
    var retention = fetchAuthoritiesRetentionDuration();

    var tillDate = LocalDateTime.now().minus(retention, ChronoUnit.DAYS);
    try (Stream<AuthorityArchive> archives = authorityArchiveRepository.streamByUpdatedTillDate(tillDate)) {
      archives.forEach(authorityArchiveService::delete);
    }
  }

  private Integer fetchAuthoritiesRetentionDuration() {
    Optional<SettingsClient.SettingEntry> expireSetting = settingsService.getAuthorityExpireSetting();
    if (expireSetting.isEmpty()) {
      log.warn("No Retention setting was defined for Authorities Expiration, using the default one: {}",
          authorityArchiveProperties.getRetentionPeriodInDays());
      return authorityArchiveProperties.getRetentionPeriodInDays();
    }

    var settingValue = expireSetting.get().value();
    log.debug("Fetched authority archives retention period (in days): {}", settingValue.retentionInDays());
    return settingValue.retentionInDays();
  }
}
