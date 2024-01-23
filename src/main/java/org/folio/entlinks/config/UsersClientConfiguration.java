package org.folio.entlinks.config;

import org.folio.spring.client.UsersClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "folio.system-user.enabled", havingValue = "false", matchIfMissing = true)
@ConditionalOnMissingBean(UsersClient.class)
@EnableFeignClients(clients = {UsersClient.class})
public class UsersClientConfiguration {
}
