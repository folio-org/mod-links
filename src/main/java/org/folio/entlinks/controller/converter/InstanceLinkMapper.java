package org.folio.entlinks.controller.converter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.qm.domain.dto.InstanceLinkDto;
import org.folio.qm.domain.dto.InstanceLinkDtoCollection;
import org.folio.qm.domain.dto.LinksCountDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstanceLinkMapper {

  InstanceLinkDto convert(InstanceAuthorityLink source);

  InstanceAuthorityLink convert(InstanceLinkDto source);

  default InstanceLinkDtoCollection convert(List<InstanceAuthorityLink> source) {
    var convertedLinks = source.stream().map(this::convert).toList();

    return new InstanceLinkDtoCollection()
      .links(convertedLinks)
      .totalRecords(source.size());
  }

  default List<LinksCountDto> convert(Map<UUID, Long> source) {
    return source.entrySet().stream()
      .map(e -> new LinksCountDto().id(e.getKey()).totalLinks(e.getValue()))
      .toList();
  }
}
