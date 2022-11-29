package org.folio.entlinks.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.projection.LinkCountView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstanceLinkRepository extends JpaRepository<InstanceAuthorityLink, Long> {

  List<InstanceAuthorityLink> findByInstanceId(UUID instanceId);

  Page<InstanceAuthorityLink> findByAuthorityId(UUID instanceId, Pageable pageable);

  List<InstanceAuthorityLink> findByAuthorityIdIn(List<UUID> authorityIds);

  void deleteByAuthorityIdIn(List<UUID> authorityIds);

  @Query("SELECT il.authorityId AS id,"
    + " COUNT(DISTINCT il.instanceId) AS totalLinks"
    + " FROM InstanceAuthorityLink il WHERE il.authorityId IN :ids"
    + " GROUP BY id ORDER BY totalLinks DESC")
  List<LinkCountView> countLinksByAuthorityIds(@Param("ids") Set<UUID> ids);

  @Modifying
  @Query("UPDATE InstanceAuthorityLink il SET il.authorityNaturalId = :naturalId, il.bibRecordSubfields = :subfields "
    + "WHERE il.authorityId = :authorityId AND il.bibRecordTag = :tag")
  void updateSubfieldsAndNaturalId(char[] subfields, String naturalId, UUID authorityId, String tag);

  @Modifying
  @Query("UPDATE InstanceAuthorityLink il SET il.authorityNaturalId = :naturalId WHERE il.authorityId = :authorityId")
  void updateNaturalId(String naturalId, UUID authorityId);
}
