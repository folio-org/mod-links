package org.folio.entlinks.service.authority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.folio.entlinks.domain.entity.Authority;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface AuthorityServiceI<T> {

  Page<T> getAll(Integer offset, Integer limit, String cql);

  Page<UUID> getAllIds(Integer offset, Integer limit, String cql);

  Map<UUID, T> getAllByIds(Collection<UUID> ids);

  T getById(UUID id);

  T create(T entity);

  T create(T entity, Consumer<T> authorityCallback);

  T update(T modified);

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  Authority update(Authority modified, boolean forced);

  T update(T modified, BiConsumer<T, T> authorityCallback);

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  Authority update(Authority modified, boolean forced,
                   BiConsumer<Authority, Authority> authorityConsumer);

  List<T> upsert(List<T> entities, Consumer<T> authorityCreateCallback, BiConsumer<T, T> authorityUpdateCallback);

  void deleteById(UUID id);

  @Transactional
  void deleteById(UUID id, boolean forced);

  void deleteById(UUID id, Consumer<T> authorityCallback);

  void deleteByIds(Collection<UUID> ids);
}
