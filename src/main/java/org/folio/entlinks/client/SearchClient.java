package org.folio.entlinks.client;

import org.folio.entlinks.domain.dto.AuthoritySearchResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("search")
public interface SearchClient {

  @GetMapping("/authorities")
  AuthoritySearchResult searchAuthorities(@RequestParam String query,
                                          @RequestParam boolean includeNumberOfTitles);
}
