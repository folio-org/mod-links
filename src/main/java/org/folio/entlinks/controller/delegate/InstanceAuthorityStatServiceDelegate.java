package org.folio.entlinks.controller.delegate;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.entlinks.client.AuthoritySourceFileClient;
import org.folio.entlinks.client.AuthoritySourceFileClient.AuthoritySourceFile;
import org.folio.entlinks.controller.converter.AuthorityDataStatMapper;
import org.folio.entlinks.domain.dto.AuthorityChangeStatDtoCollection;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.dto.AuthorityDataStatDto;
import org.folio.entlinks.domain.dto.Metadata;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.folio.entlinks.integration.internal.AuthoritySourceFilesService;
import org.folio.entlinks.service.links.AuthorityDataStatService;
import org.folio.entlinks.utils.DateUtils;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.client.UsersClient.User;
import org.folio.spring.tools.model.ResultList;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class InstanceAuthorityStatServiceDelegate {

  private final AuthorityDataStatService dataStatService;
  private final AuthoritySourceFilesService sourceFilesService;

  private final AuthorityDataStatMapper dataStatMapper;
  private final UsersClient usersClient;

  public AuthorityChangeStatDtoCollection fetchAuthorityLinksStats(OffsetDateTime fromDate, OffsetDateTime toDate,
                                                                   AuthorityDataStatActionDto action, Integer limit) {
    List<AuthorityDataStat> dataStatList = dataStatService.fetchDataStats(fromDate, toDate, action, limit + 1);

    Optional<AuthorityDataStat> last = Optional.empty();
    if (dataStatList.size() > limit) {
      last = Optional.of(dataStatList.get(limit));
      last.ifPresent(dataStatList::remove);
    }

    Map<UUID, AuthoritySourceFileClient.AuthoritySourceFile> sourceFilesMap =
      sourceFilesService.fetchAuthoritySources();

    String query = getUsersQueryString(dataStatList);
    ResultList<User> userResultList =
      query.isEmpty() ? ResultList.of(0, Collections.emptyList()) : usersClient.query(query);
    var stats = dataStatList
      .stream()
      .map(source -> getAuthorityDataStatDto(sourceFilesMap, userResultList, source))
      .toList();

    return new AuthorityChangeStatDtoCollection()
      .stats(stats)
      .next(last.map(authorityDataStat -> DateUtils.fromTimestamp(authorityDataStat.getStartedAt()))
        .orElse(null));
  }

  protected AuthorityDataStatDto getAuthorityDataStatDto(Map<UUID, AuthoritySourceFile> sourceFilesMap,
                                                         ResultList<User> userResultList,
                                                         AuthorityDataStat source) {

    var authorityDataStatDto = dataStatMapper.convertToDto(source);

    if (authorityDataStatDto != null && authorityDataStatDto.getSourceFileNew() != null) {
      var sourceFile = sourceFilesMap.get(UUID.fromString(authorityDataStatDto.getSourceFileNew()));
      authorityDataStatDto.setMetadata(getMetadata(userResultList, source));

      if (sourceFile != null) {
        authorityDataStatDto.setSourceFileNew(sourceFile.name());
      } else {
        // keep original value authSourceFileId
        log.warn("AuthoritySourceFile not found by [sourceFileId={}]", authorityDataStatDto.getSourceFileNew());
      }
    }
    return authorityDataStatDto;
  }


  public Metadata getMetadata(ResultList<User> userResultList, AuthorityDataStat source) {
    if (userResultList == null || source == null) {
      log.debug("getMetadata:: Attempts to return null, empty input params");
      return null;
    }

    try {
      var user = userResultList.getResult()
        .stream()
        .filter(Objects::nonNull)
        .filter(u -> UUID.fromString(u.id()).equals(source.getStartedByUserId()))
        .findFirst().orElseThrow(RuntimeException::new);

      Metadata metadata = new Metadata();
      metadata.setStartedByUserFirstName(user.personal().firstName());
      metadata.setStartedByUserLastName(user.personal().lastName());
      metadata.setStartedByUserId(UUID.fromString(user.id()));
      metadata.setStartedAt(DateUtils.fromTimestamp(source.getStartedAt()));
      metadata.setCompletedAt(DateUtils.fromTimestamp(source.getCompletedAt()));
      return metadata;

    } catch (RuntimeException e) {
      log.warn("getMetadata:: User not found by given id: {}. Attempts to return null",
        source.getStartedByUserId());
      return null;
    }
  }

  private String getUsersQueryString(List<AuthorityDataStat> dataStatList) {
    var userIds = dataStatList.stream()
      .map(AuthorityDataStat::getStartedByUserId)
      .filter(Objects::nonNull)
      .map(UUID::toString)
      .distinct()
      .collect(Collectors.joining(" or "));
    return userIds.isEmpty() ? "" : "id=(" + userIds + ")";
  }
}
