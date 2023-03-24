package org.folio.entlinks.controller.converter;

import org.folio.entlinks.domain.dto.AuthorityStatsDto;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorityDataStatMapper {

  @Mapping(target = "authorityId", source = "source.authorityData.id")
  @Mapping(target = "naturalIdOld", source = "authorityNaturalIdOld")
  @Mapping(target = "naturalIdNew", source = "authorityNaturalIdNew")
  @Mapping(target = "sourceFileOld", source = "authoritySourceFileOld")
  @Mapping(target = "sourceFileNew", source = "authoritySourceFileNew")
  AuthorityStatsDto convertToDto(AuthorityDataStat source);

}
