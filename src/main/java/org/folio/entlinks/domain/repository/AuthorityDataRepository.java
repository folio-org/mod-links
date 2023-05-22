package org.folio.entlinks.domain.repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorityDataRepository extends JpaRepository<AuthorityData, UUID> {

  @Modifying
  @Query("update AuthorityData a set a.naturalId = :naturalId where a.id = :id")
  void updateNaturalIdById(@Param("naturalId") String naturalId, @Param("id") UUID id);

  @Modifying
  @Query("update AuthorityData a set a.deleted = true where a.id in :ids")
  void updateDeletedByIdIn(@Param("ids") Collection<UUID> ids);

  @Query("select a.id from AuthorityData a where a.naturalId in :naturalIds")
  Set<UUID> findIdsByNaturalIds(@Param("naturalIds") Collection<String> naturalIds);
}
