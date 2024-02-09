package org.folio.entlinks.domain.repository;

import jakarta.persistence.EntityManager;
import org.folio.entlinks.domain.entity.AuthorityArchive;

public class AuthorityArchiveCqlRepositoryImpl extends AuthorityBaseCqlRepositoryImpl<AuthorityArchive>
  implements AuthorityArchiveCqlRepository {

  public AuthorityArchiveCqlRepositoryImpl(EntityManager em) {
    super(em);
  }

  @Override
  protected Class<AuthorityArchive> getClassType() {
    return AuthorityArchive.class;
  }

  @Override
  protected Boolean deleted() {
    return Boolean.TRUE;
  }
}
