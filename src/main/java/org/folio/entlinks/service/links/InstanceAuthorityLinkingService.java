package org.folio.entlinks.service.links;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.entlinks.domain.projection.LinkCountView;
import org.folio.entlinks.repository.InstanceLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstanceAuthorityLinkingService {

  private final InstanceLinkRepository repository;

  public List<InstanceAuthorityLink> getLinks(UUID instanceId) {
    return repository.findByInstanceId(instanceId);
  }

  @Transactional
  public void updateLinks(UUID instanceId, List<InstanceAuthorityLink> incomingLinks) {
    var existedLinks = repository.findByInstanceId(instanceId);

    var linksToDelete = subtract(existedLinks, incomingLinks);
    var linksToSave = getLinksToSave(incomingLinks, existedLinks, linksToDelete);
    repository.deleteAllInBatch(linksToDelete);
    repository.saveAll(linksToSave);
  }

  public Map<UUID, Long> countLinksByAuthorityIds(Set<UUID> authorityIds) {
    return repository.countLinksByAuthorityIds(authorityIds).stream()
      .collect(Collectors.toMap(LinkCountView::getId, LinkCountView::getTotalLinks));
  }

  public Set<UUID> retainAuthoritiesIdsWithLinks(Set<UUID> authorityIds) {
    var authorityIdsWithLinks = repository.findAuthorityIdsWithLinks(authorityIds);
    var result = new HashSet<>(authorityIds);
    result.retainAll(authorityIdsWithLinks);
    return result;
  }

  @Transactional
  public void updateNaturalId(String naturalId, UUID authorityId) {
    repository.updateNaturalId(naturalId, authorityId);
  }

  @Transactional
  public void updateSubfieldsAndNaturalId(char[] subfields, String naturalId, UUID authorityId, String tag) {
    repository.updateSubfieldsAndNaturalId(subfields, naturalId, authorityId, tag);
  }

  @Transactional
  public void deleteByAuthorityIdIn(List<UUID> authorityIds) {
    repository.deleteByAuthorityIdIn(authorityIds);
  }

  private List<InstanceAuthorityLink> getLinksToSave(List<InstanceAuthorityLink> incomingLinks,
                                                     List<InstanceAuthorityLink> existedLinks,
                                                     List<InstanceAuthorityLink> linksToDelete) {
    var linksToCreate = subtract(incomingLinks, existedLinks);
    var linksToUpdate = subtract(existedLinks, linksToDelete);
    updateLinksData(incomingLinks, linksToUpdate);
    var linksToSave = new ArrayList<>(linksToCreate);
    linksToSave.addAll(linksToUpdate);
    return linksToSave;
  }

  private void updateLinksData(List<InstanceAuthorityLink> incomingLinks, List<InstanceAuthorityLink> linksToUpdate) {
    linksToUpdate
      .forEach(link -> incomingLinks.stream().filter(l -> l.isSameLink(link)).findFirst()
        .ifPresent(l -> {
          link.setAuthorityNaturalId(l.getAuthorityNaturalId());
          link.setBibRecordSubfields(l.getBibRecordSubfields());
        }));
  }

  private List<InstanceAuthorityLink> subtract(Collection<InstanceAuthorityLink> source,
                                               Collection<InstanceAuthorityLink> target) {
    return new LinkedHashSet<>(source).stream()
      .filter(t -> target.stream().noneMatch(link -> link.isSameLink(t)))
      .toList();
  }

}
