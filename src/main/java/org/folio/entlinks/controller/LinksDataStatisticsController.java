package org.folio.entlinks.controller;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.folio.entlinks.controller.delegate.InstanceAuthorityStatServiceDelegate;
import org.folio.entlinks.controller.delegate.LinkingServiceDelegate;
import org.folio.entlinks.domain.dto.DataStatsDtoCollection;
import org.folio.entlinks.domain.dto.DataStatsType;
import org.folio.entlinks.domain.dto.LinkAction;
import org.folio.entlinks.domain.dto.LinkStatus;
import org.folio.entlinks.rest.resource.LinksDataStatisticsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LinksDataStatisticsController implements LinksDataStatisticsApi {

  private final InstanceAuthorityStatServiceDelegate instanceAuthorityStatServiceDelegate;

  private final LinkingServiceDelegate linkingServiceDelegate;


  @Override
  public ResponseEntity<DataStatsDtoCollection> getLinksDataStats(String dataStatsType,
                                                                  OffsetDateTime fromDate, OffsetDateTime toDate,
                                                                  LinkAction action, LinkStatus status,
                                                                  Integer limit) {
    return ResponseEntity.ok(
      switch (DataStatsType.fromValue(dataStatsType)) {
        case INSTANCE -> linkingServiceDelegate
          .getLinkedBibUpdateStats(status, fromDate, toDate, limit);
        case AUTHORITY -> instanceAuthorityStatServiceDelegate
          .fetchAuthorityLinksStats(fromDate, toDate, action, limit);
      }
    );
  }
}

