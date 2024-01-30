package org.folio.entlinks.domain.repository;

import jakarta.persistence.EntityManager;
import org.folio.entlinks.domain.entity.Authority;

public class AuthorityCqlRepositoryImpl extends AuthorityBaseCqlRepositoryImpl<Authority>
  implements AuthorityCqlRepository {

  public AuthorityCqlRepositoryImpl(EntityManager em) {
    super(em);
  }

  @Override
  protected Class<Authority> getClassType() {
    return Authority.class;
  }

  @Override
  protected Boolean deleted() {
    return Boolean.FALSE;
  }
}
