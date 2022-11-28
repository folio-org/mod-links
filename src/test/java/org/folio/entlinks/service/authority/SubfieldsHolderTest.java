package org.folio.entlinks.service.authority;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.folio.qm.domain.dto.LinksEventSubfields;
import org.folio.qm.domain.dto.SubfieldModification;
import org.hamcrest.Matchers;
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
    var linkingRuleDto = new LinkingRuleDto();
    linkingRuleDto.setAuthorityField("100");
    linkingRuleDto.setBibField("240");
    linkingRuleDto.setAuthoritySubfields(List.of("f", "g", "h", "k", "l", "m", "n", "o", "p", "r", "s", "t"));
    linkingRuleDto.setSubfieldModifications(List.of(new SubfieldModification().source("t").target("a")));

    var subfieldsHolder = new SubfieldsHolder(dataField, linkingRuleDto);

    assertThat(subfieldsHolder.getBibSubfieldCodes(), Matchers.hasItems("a", "h"));
    assertThat(subfieldsHolder.toSubfieldsChange(), Matchers.hasItems(
      new LinksEventSubfields().code("a").value("t-data"),
      new LinksEventSubfields().code("h").value("h-data")
    ));
  }
}
