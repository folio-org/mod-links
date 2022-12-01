package org.folio.entlinks.service.messaging.authority.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.entlinks.domain.dto.SubfieldChange;
import org.folio.entlinks.domain.dto.SubfieldModification;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

class FieldChangeHolderTest {

  @Test
  void testSubfieldsThatRequireModifications() {
    var dataField = new DataFieldImpl("100", '0', '0');
    dataField.addSubfield(new SubfieldImpl('a', "a-data"));
    dataField.addSubfield(new SubfieldImpl('d', "d-data"));
    dataField.addSubfield(new SubfieldImpl('h', "h-data"));
    dataField.addSubfield(new SubfieldImpl('t', "t-data"));
    var linkingRuleDto = new InstanceAuthorityLinkingRule();
    linkingRuleDto.setAuthorityField("100");
    linkingRuleDto.setBibField("240");
    linkingRuleDto.setAuthoritySubfields(new char[] {'f', 'g', 'h', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't'});
    linkingRuleDto.setSubfieldModifications(List.of(new SubfieldModification().source("t").target("a")));

    var subfieldsHolder = new FieldChangeHolder(dataField, linkingRuleDto);

    assertThat(subfieldsHolder.getBibSubfieldCodes()).contains('a', 'h');
    assertThat(subfieldsHolder.toSubfieldsChange()).contains(
      new SubfieldChange().code("a").value("t-data"),
      new SubfieldChange().code("h").value("h-data")
    );
  }
}
