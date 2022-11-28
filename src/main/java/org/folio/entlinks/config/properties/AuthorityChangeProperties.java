package org.folio.entlinks.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("folio.authority.change")
public class AuthorityChangeProperties {

  private int partitionSize = 100;

}
