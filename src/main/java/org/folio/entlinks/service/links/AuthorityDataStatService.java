package org.folio.entlinks.service.links;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.entlinks.domain.dto.LinkUpdateReport.StatusEnum.FAIL;
import static org.folio.entlinks.domain.dto.LinkUpdateReport.StatusEnum.SUCCESS;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.entlinks.domain.dto.LinkUpdateReport;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.folio.entlinks.domain.entity.AuthorityDataStatStatus;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkStatus;
import org.folio.entlinks.domain.repository.AuthorityDataStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorityDataStatService {

  private static final String FAIL_CAUSE_DELIMITER = " | ";

  private final AuthorityDataStatRepository statRepository;

  private final AuthorityDataService authorityDataService;
  private final InstanceAuthorityLinkingService linkingService;

  public List<AuthorityDataStat> createInBatch(List<AuthorityDataStat> stats) {
    var authorityDataSet = stats.stream()
      .map(AuthorityDataStat::getAuthorityData)
      .collect(Collectors.toSet());
    var savedAuthorityData = authorityDataService.saveAll(authorityDataSet);

    for (AuthorityDataStat stat : stats) {
      stat.setId(UUID.randomUUID());
      stat.setStatus(AuthorityDataStatStatus.IN_PROGRESS);
      var authorityData = savedAuthorityData.get(stat.getAuthorityData().getId());
      stat.setAuthorityData(authorityData);
    }

    return statRepository.saveAll(stats);
  }

  @Transactional
  public void updateForReports(UUID jobId, List<LinkUpdateReport> reports) {
    log.info("Updating links, stats for reports: [jobId: {}, reports count: {}]", jobId, reports.size());
    log.debug("Updating links,stats for reports: [reports: {}]", reports);
    updateLinks(reports);
    updateStatsData(jobId, reports);
  }

  private void updateLinks(List<LinkUpdateReport> reports) {
    reports.forEach(report -> {
      var linkIds = report.getLinkIds();
      var links = linkingService.getLinksByIds(linkIds);

      links.forEach(link -> {
        link.setStatus(mapReportStatus(report));
        link.setUpdatedAt(now());
        if (report.getStatus().equals(FAIL)) {
          link.setErrorCause(report.getFailCause());
        } else {
          link.setErrorCause(EMPTY);
        }
      });

      linkingService.saveAll(report.getInstanceId().toString(), links);
    });
  }

  private InstanceAuthorityLinkStatus mapReportStatus(LinkUpdateReport report) {
    switch (report.getStatus()) {
      case SUCCESS -> {
        return InstanceAuthorityLinkStatus.ACTUAL;
      }
      case FAIL -> {
        return InstanceAuthorityLinkStatus.ERROR;
      }
      default -> throw new IllegalArgumentException("Unknown link update report status.");
    }
  }

  /**
   * Updates authority statistics data.

   * @param jobId linked bib update job id.
   *              AuthorityDataStat id and jobId are interchangeable (jobId is used as id to create stat record)
   * */
  private void updateStatsData(UUID jobId, List<LinkUpdateReport> reports) {
    var dataStat = statRepository.findById(jobId)
      .orElseThrow(() -> new IllegalStateException("Cannot find authority data statistics for id: " + jobId));
    var failedCount = getReportCountForStatus(reports, FAIL);
    var successCount = getReportCountForStatus(reports, SUCCESS);

    if (failedCount != 0) {
      dataStat.setFailCause(getStatFailCause(dataStat, reports));
    }
    dataStat.setLbUpdated(dataStat.getLbUpdated() + successCount);
    dataStat.setLbFailed(dataStat.getLbFailed() + failedCount);

    var jobCompleted = dataStat.getLbUpdated() + dataStat.getLbFailed() == dataStat.getLbTotal();
    if (jobCompleted) {
      dataStat.setCompletedAt(now());
      updateStatStatus(dataStat);
    }

    log.info("Saving stats data [statsId: {}, status: {}]", dataStat.getId(), dataStat.getStatus());
    log.debug("Stats data: {}", dataStat);
    statRepository.save(dataStat);
  }

  private int getReportCountForStatus(List<LinkUpdateReport> reports, LinkUpdateReport.StatusEnum status) {
    return (int) reports.stream()
      .filter(linkUpdateReport -> linkUpdateReport.getStatus().equals(status))
      .count();
  }

  private String getStatFailCause(AuthorityDataStat dataStat, List<LinkUpdateReport> reports) {
    var newFailCause = reports.stream()
      .map(LinkUpdateReport::getFailCause)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.joining(FAIL_CAUSE_DELIMITER));

    if (isBlank(newFailCause)) {
      return dataStat.getFailCause();
    }

    if (isBlank(dataStat.getFailCause())) {
      return newFailCause;
    }

    return dataStat.getFailCause() + FAIL_CAUSE_DELIMITER + newFailCause;
  }

  private void updateStatStatus(AuthorityDataStat dataStat) {
    if (dataStat.getLbFailed() == 0) {
      dataStat.setStatus(AuthorityDataStatStatus.COMPLETED_SUCCESS);
    } else if (dataStat.getLbFailed() == dataStat.getLbTotal()) {
      dataStat.setStatus(AuthorityDataStatStatus.FAILED);
    } else {
      dataStat.setStatus(AuthorityDataStatStatus.COMPLETED_WITH_ERRORS);
    }
  }

  private Timestamp now() {
    return new Timestamp(System.currentTimeMillis());
  }

}
