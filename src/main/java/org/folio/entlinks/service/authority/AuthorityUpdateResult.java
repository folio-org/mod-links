package org.folio.entlinks.service.authority;

import org.folio.entlinks.domain.entity.Authority;

public record AuthorityUpdateResult(Authority oldEntity, Authority newEntity) { }
