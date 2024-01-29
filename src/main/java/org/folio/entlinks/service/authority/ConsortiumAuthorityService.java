package org.folio.entlinks.service.authority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.domain.repository.authority.AuthorityRepository;
import org.folio.entlinks.exception.ConsortiumIllegalActionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("consortiumAuthorityService")
public class ConsortiumAuthorityService extends AuthorityService {

  private final AuthorityRepository repository;

  public ConsortiumAuthorityService(AuthorityRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Authority update(Authority modified) {
    return update(modified, false);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Authority update(Authority modified, boolean forced) {
    return update(modified, forced, null);
  }

  @Override
  public Authority update(Authority modified, BiConsumer<Authority, Authority> authorityConsumer) {
    return update(modified, false, authorityConsumer);
  }

  @Override
  public Authority update(Authority modified, boolean forced, BiConsumer<Authority, Authority> authorityConsumer) {
    if (!forced) {
      validate(modified.getId(), "Update");
    }
    return super.update(modified, forced, authorityConsumer);
  }

  @Override
  public List<Authority> upsert(List<Authority> authorities, Consumer<Authority> authorityCreateCallback,
                                BiConsumer<Authority, Authority> authorityUpdateCallback) {
    return super.upsert(authorities, authorityCreateCallback, authorityUpdateCallback);
  }

  @Override
  public void deleteById(UUID id) {
    deleteById(id, false);
  }

  @Override
  public void deleteById(UUID id, boolean forced) {
    deleteById(id, forced, null);
  }

  @Override
  public void deleteById(UUID id, Consumer<Authority> authorityCallback) {
    deleteById(id, false, authorityCallback);
  }

  @Override
  public void deleteById(UUID id, boolean forced, Consumer<Authority> authorityCallback) {
    if (!forced) {
      validate(id, "Delete");
    }
    super.deleteById(id, forced, authorityCallback);
  }

  @Override
  public void deleteByIds(Collection<UUID> ids) {
    super.deleteByIds(ids);
  }

  private void validate(UUID id, String actionName) {
    repository.findByIdAndDeletedFalse(id)
      .flatMap(authority -> authority.isConsortiumShadowCopy() ? Optional.empty() : Optional.of(authority))
      .orElseThrow(() -> new ConsortiumIllegalActionException(actionName));
  }
}
