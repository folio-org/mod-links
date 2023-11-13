package org.folio.entlinks.controller.delegate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.entlinks.client.SettingsClient;
import org.folio.entlinks.config.properties.AuthorityArchiveProperties;
import org.folio.entlinks.domain.entity.AuthorityArchive;
import org.folio.entlinks.domain.repository.AuthorityArchiveRepository;
import org.folio.entlinks.integration.SettingsService;
import org.folio.entlinks.service.authority.AuthorityArchiveService;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthorityArchiveServiceDelegateTest {

  @Mock
  private AuthorityArchiveService authorityArchiveService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private AuthorityArchiveRepository authorityArchiveRepository;

  @Mock
  private AuthorityArchiveProperties authorityArchiveProperties;

  @InjectMocks
  private AuthorityArchiveServiceDelegate delegate;

  @Test
  void shouldExpireAuthorityArchivesWithDefaultRetentionPeriod() {
    var archive = new AuthorityArchive();
    archive.setUpdatedDate(Timestamp.from(Instant.now().minus(10, ChronoUnit.DAYS)));
    when(settingsService.getAuthorityExpireSetting()).thenReturn(Optional.empty());
    when(authorityArchiveProperties.getRetentionPeriodInDays()).thenReturn(7);
    when(authorityArchiveRepository.streamByUpdatedTillDate(any(LocalDateTime.class))).thenReturn(Stream.of(archive));

    delegate.expire();

    verify(authorityArchiveService).delete(archive);
  }

  @Test
  void shouldExpireAuthorityArchivesWithRetentionPeriodFromSettings() {
    var archive = new AuthorityArchive();
    var setting = new SettingsClient.SettingEntry(UUID.randomUUID(), "mod-entities-links",
        "authority-archives-retention", new SettingsClient.AuthoritiesRetentionSettingValue(1));
    archive.setUpdatedDate(Timestamp.from(Instant.now().minus(2, ChronoUnit.DAYS)));
    when(settingsService.getAuthorityExpireSetting()).thenReturn(Optional.of(setting));
    when(authorityArchiveRepository.streamByUpdatedTillDate(any(LocalDateTime.class))).thenReturn(Stream.of(archive));

    delegate.expire();

    verify(authorityArchiveService).delete(archive);
  }
}
