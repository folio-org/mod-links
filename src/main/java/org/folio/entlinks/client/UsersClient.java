package org.folio.entlinks.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.folio.spring.tools.model.ResultList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("users")
public interface UsersClient {
  @GetMapping
  ResultList<User> query(@RequestParam("query") String query);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record User(String id, String username, boolean active,
              User.Personal personal) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Personal(String firstName, String lastName) {
    }
  }
}
