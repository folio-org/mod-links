package org.folio.entlinks.controller.delegate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.controller.converter.AuthorityDataStatMapper;
import org.folio.entlinks.domain.dto.AuthorityChangeStatDtoCollection;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.folio.entlinks.service.links.AuthorityDataStatService;
import org.folio.entlinks.utils.DateUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InstanceAuthorityStatServiceDelegate {

  private final AuthorityDataStatService dataStatService;
  private final AuthorityDataStatMapper dataStatMapper;

  public AuthorityChangeStatDtoCollection fetchAuthorityLinksStats(OffsetDateTime fromDate, OffsetDateTime toDate,
                                                                   AuthorityDataStatActionDto action, Integer limit) {
    List<AuthorityDataStat> dataStatList = dataStatService.fetchDataStats(fromDate, toDate, action, limit + 1);

    Optional<AuthorityDataStat> last = Optional.empty();
    if (dataStatList.size() > limit) {
      last = Optional.of(dataStatList.get(limit));
    }

    var stats = dataStatList.stream().limit(limit)
      .map(dataStatMapper::convertToDto)
      .toList();

    return new AuthorityChangeStatDtoCollection()
      .stats(stats)
      .next(last.map(authorityDataStat -> DateUtils.fromTimestamp(authorityDataStat.getStartedAt()))
        .orElse(null));
  }
}
