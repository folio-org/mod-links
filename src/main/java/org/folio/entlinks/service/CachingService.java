package org.folio.entlinks.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachingService {

  private final CacheManager cacheManager;

  public void invalidateCache(String cacheName) {
    if (StringUtils.isNotBlank(cacheName)) {
      Optional.ofNullable(cacheManager.getCache(cacheName))
        .ifPresent(Cache::clear);
    }
  }
}
