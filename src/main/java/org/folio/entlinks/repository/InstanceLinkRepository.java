package org.folio.entlinks.repository;

import java.util.List;
import java.util.UUID;
import org.folio.entlinks.model.entity.InstanceLink;
import org.folio.entlinks.model.projection.LinkCountView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstanceLinkRepository extends JpaRepository<InstanceLink, Long> {

  List<InstanceLink> findByInstanceId(UUID instanceId);

  Page<InstanceLink> findByAuthorityId(UUID instanceId, Pageable pageable);

  List<InstanceLink> findByAuthorityIdIn(List<UUID> authorityIds);

  void deleteByAuthorityIdIn(List<UUID> authorityIds);

  @Query("SELECT il.authorityId AS id,"
      + " COUNT(DISTINCT il.instanceId) AS totalLinks"
      + " FROM InstanceLink il WHERE il.authorityId IN :ids"
      + " GROUP BY id ORDER BY totalLinks DESC")
  List<LinkCountView> countLinksByAuthorityIds(@Param("ids") List<UUID> ids);

  @Modifying
  @Query(value = "UPDATE instance_link SET bib_record_subfields = ?1, "
    + "authority_natural_id = ?2 "
    + "WHERE authority_id = ?3 AND bib_record_tag = ?4", nativeQuery = true)
  void updateSubfieldsAndNaturalId(String[] subfields, String naturalId, UUID authorityId, String tag);

  @Modifying
  @Query("UPDATE InstanceLink il SET il.authorityNaturalId = :naturalId WHERE il.authorityId = :authorityId")
  void updateNaturalId(String naturalId, UUID authorityId);
}
