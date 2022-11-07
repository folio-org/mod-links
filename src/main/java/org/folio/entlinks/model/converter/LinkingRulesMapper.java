package org.folio.entlinks.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.entlinks.LinkingRecords;
import org.folio.entlinks.model.entity.LinkingRules;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.mapstruct.Mapper;
import org.springframework.boot.json.JsonParseException;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LinkingRulesMapper {

  ObjectMapper mapper = new ObjectMapper();

  default List<LinkingRuleDto> convert(LinkingRules linkingRules) {
    try {
      var rules = mapper.readValue(linkingRules.getData(), LinkingRuleDto[].class);
      return List.of(rules);
    } catch (JsonProcessingException e) {
      throw new JsonParseException(e);
    }
  }

  default LinkingRules convert(LinkingRecords records, String jsonRules) {
    try {
      mapper.readTree(jsonRules);
      return LinkingRules.builder()
          .linkingRecords(records.name())
          .data(jsonRules)
          .build();
    } catch (JsonProcessingException e) {
      throw new JsonParseException(e);
    }
  }
}
