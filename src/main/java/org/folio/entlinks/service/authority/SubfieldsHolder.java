package org.folio.entlinks.service.authority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.folio.qm.domain.dto.LinkingRuleDto;
import org.folio.qm.domain.dto.LinksEventSubfields;
import org.folio.qm.domain.dto.SubfieldModification;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;

public class SubfieldsHolder {

  List<Subfield> subfields;
  LinkingRuleDto linkingRuleDto;
  List<Subfield> bibSubfields;

  @Getter
  List<String> bibSubfieldCodes;

  public SubfieldsHolder(DataField dataField, LinkingRuleDto linkingRuleDto) {
    this.linkingRuleDto = linkingRuleDto;
    this.subfields = dataField.getSubfields();
    this.bibSubfields = getBibSubfields(subfields);
    this.bibSubfieldCodes = bibSubfields.stream().map(Subfield::getCode).map(Objects::toString).toList();
  }

  public List<LinksEventSubfields> toSubfieldsChange() {
    return bibSubfields.stream()
      .map(subfield -> new LinksEventSubfields()
        .code(Character.toString(subfield.getCode()))
        .value(subfield.getData()))
      .sorted(Comparator.comparing(LinksEventSubfields::getCode))
      .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<Subfield> getBibSubfields(List<Subfield> subfields) {
    return subfields.stream()
      .filter(subfield -> linkingRuleDto.getAuthoritySubfields().contains(Character.toString(subfield.getCode())))
      .map(subfield -> {
        var code = getCode(subfield);
        return (Subfield) new SubfieldImpl(code, subfield.getData());
      })
      .toList();
  }

  private char getCode(Subfield subfield) {
    var code = subfield.getCode();
    for (SubfieldModification subfieldModification : linkingRuleDto.getSubfieldModifications()) {
      if (subfieldModification.getSource().charAt(0) == code) {
        return subfieldModification.getTarget().charAt(0);
      }
    }
    return code;
  }
}
