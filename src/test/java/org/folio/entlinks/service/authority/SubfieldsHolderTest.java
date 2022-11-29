package org.folio.entlinks.service.authority;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.entlinks.model.entity.InstanceAuthorityLinkingRule;
import org.folio.qm.domain.dto.LinksEventSubfields;
import org.folio.qm.domain.dto.SubfieldModification;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

class SubfieldsHolderTest {

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

    var subfieldsHolder = new SubfieldsHolder(dataField, linkingRuleDto);

    assertThat(subfieldsHolder.getBibSubfieldCodes()).contains('a', 'h');
    assertThat(subfieldsHolder.toSubfieldsChange()).contains(
      new LinksEventSubfields().code("a").value("t-data"),
      new LinksEventSubfields().code("h").value("h-data")
    );
  }
}
