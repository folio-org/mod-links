package org.folio.entlinks.service.links;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.repository.AuthorityDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorityDataService {

  private final AuthorityDataRepository repository;

  public List<AuthorityData> findByIds(Collection<UUID> ids) {
    if (log.isDebugEnabled()) {
      log.debug("Fetching authority data [authority ids: {}]", ids);
    } else {
      log.info("Fetching authority data for {} authority ids", ids.size());
    }
    return repository.findAllById(ids);
  }

  public Map<UUID, AuthorityData> saveAll(Collection<AuthorityData> authorityDataSet) {
    if (log.isDebugEnabled()) {
      log.debug("Saving authority data [authority data: {}]", authorityDataSet);
    } else {
      log.info("Saving authority data for {} authority", authorityDataSet.size());
    }
    return repository.saveAll(authorityDataSet).stream().collect(Collectors.toMap(AuthorityData::getId, ad -> ad));
  }

  @Transactional
  public void updateNaturalId(String naturalId, UUID authorityId) {
    log.info("Update authority data [authority id: {}, natural id: {}]", authorityId, naturalId);
    repository.updateNaturalIdById(naturalId, authorityId);
  }

  @Transactional
  public void markDeleted(Collection<UUID> ids) {
    repository.updateDeletedByIdIn(ids);
  }
}
