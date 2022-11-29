package org.folio.entlinks.repository;

import java.util.List;
import org.folio.entlinks.model.entity.InstanceAuthorityLinkingRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkingRulesRepository extends JpaRepository<InstanceAuthorityLinkingRule, String> {

  List<InstanceAuthorityLinkingRule> findByAuthorityField(String authorityField);
}
