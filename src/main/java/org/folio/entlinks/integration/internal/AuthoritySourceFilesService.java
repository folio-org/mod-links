package org.folio.entlinks.integration.internal;

import static java.util.Objects.nonNull;
import static org.folio.entlinks.config.constants.CacheNames.AUTHORITY_SOURCE_FILES_CACHE;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.AuthoritySourceFileClient;
import org.folio.entlinks.client.AuthoritySourceFileClient.AuthoritySourceFile;
import org.folio.entlinks.exception.FolioIntegrationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthoritySourceFilesService {
  private static final int SOURCE_FILES_LIMIT = 100;
  private final AuthoritySourceFileClient client;

  @Cacheable(cacheNames = AUTHORITY_SOURCE_FILES_CACHE,
    key = "@folioExecutionContext.tenantId",
    unless = "#result.isEmpty()")
  public Map<UUID, AuthoritySourceFile> fetchAuthoritySources() throws FolioIntegrationException {
    return fetchAuthoritySourceFiles().stream()
      .filter(file -> nonNull(file.id()) && nonNull(file.baseUrl()))
      .collect(Collectors.toMap(AuthoritySourceFile::id, file -> file));
  }

  @Cacheable(cacheNames = AUTHORITY_SOURCE_FILES_CACHE,
    key = "@folioExecutionContext.tenantId + ':' + #naturalId",
    unless = "#result.isEmpty()")
  public AuthoritySourceFile fetchAuthoritySourceFile(String naturalId) {
    return fetchAuthoritySourceFiles().stream()
      .filter(file -> file.codes().stream().anyMatch(naturalId::startsWith))
      .findFirst()
      .orElse(null);
  }

  private List<AuthoritySourceFile> fetchAuthoritySourceFiles() {
    log.info("Fetching authority source files");
    try {
      var authoritySourceFiles = client
        .fetchAuthoritySourceFiles(SOURCE_FILES_LIMIT)
        .authoritySourceFiles();

      if (authoritySourceFiles.isEmpty()) {
        throw new FolioIntegrationException("Authority source files are empty");
      }
      return authoritySourceFiles;
    } catch (FolioIntegrationException e) {
      log.warn(e.getMessage());
      throw new FolioIntegrationException(e.getMessage());
    } catch (Exception e) {
      log.warn("Failed to fetch authority source files");
      throw new FolioIntegrationException("Failed to fetch authority source files", e);
    }
  }
}
