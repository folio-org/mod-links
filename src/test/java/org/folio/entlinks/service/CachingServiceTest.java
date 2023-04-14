package org.folio.entlinks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class CachingServiceTest {

  private @Mock CacheManager cacheManager;
  private @Mock Cache cache;
  private @InjectMocks CachingService cachingService;

  @Test
  void invalidateCache_WithNonNullCacheName_ShouldClearCache() {
    // given
    String cacheName = "testCacheName";

    when(cacheManager.getCache(cacheName)).thenReturn(cache);

    // when
    cachingService.invalidateCache(cacheName);

    // then
    verify(cacheManager, times(1)).getCache(cacheName);
    verify(cache, times(1)).clear();
  }

  @Test
  void invalidateCache_WithNullCacheName_ShouldNotClearCache() {
    // given
    String cacheName = null;

    // when
    cachingService.invalidateCache(cacheName);

    // then
    verify(cacheManager, never()).getCache(any());
    verify(cache, never()).clear();
  }

  @Test
  void invalidateCache_WithEmptyCacheName_ShouldNotClearCache() {
    // given
    String cacheName = "";

    // when
    cachingService.invalidateCache(cacheName);

    // then
    verify(cacheManager, never()).getCache(any());
    verify(cache, never()).clear();
  }

  @Test
  void invalidateCache_WithNonExistingCacheName_ShouldNotClearCache() {
    // given
    String cacheName = "testCacheName";

    when(cacheManager.getCache(cacheName)).thenReturn(null);

    // when
    cachingService.invalidateCache(cacheName);

    // then
    verify(cacheManager, times(1)).getCache(cacheName);
    verify(cache, never()).clear();
  }
}
