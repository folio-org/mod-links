package org.folio.entlinks.service.messaging.authority.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bouncycastle.util.Arrays;
import org.folio.entlinks.domain.dto.SubfieldChange;
import org.folio.entlinks.domain.dto.SubfieldModification;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;

public class SubfieldsHolder {

  InstanceAuthorityLinkingRule linkingRuleDto;

  List<Subfield> subfields;
  List<Subfield> bibSubfields;

  @Getter
  char[] bibSubfieldCodes;

  public SubfieldsHolder(DataField dataField, InstanceAuthorityLinkingRule linkingRuleDto) {
    this.linkingRuleDto = linkingRuleDto;
    this.subfields = dataField.getSubfields();
    this.bibSubfields = getBibSubfields(subfields);
    this.bibSubfieldCodes = new char[bibSubfields.size()];
    for (int i = 0; i < bibSubfields.size(); i++) {
      bibSubfieldCodes[i] = bibSubfields.get(i).getCode();
    }
  }

  public List<SubfieldChange> toSubfieldsChange() {
    return bibSubfields.stream()
      .map(subfield -> new SubfieldChange()
        .code(Character.toString(subfield.getCode()))
        .value(subfield.getData()))
      .sorted(Comparator.comparing(SubfieldChange::getCode))
      .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<Subfield> getBibSubfields(List<Subfield> subfields) {
    return subfields.stream()
      .filter(subfield -> Arrays.contains(linkingRuleDto.getAuthoritySubfields(), subfield.getCode()))
      .map(subfield -> {
        var code = getCode(subfield);
        return (Subfield) new SubfieldImpl(code, subfield.getData());
      })
      .sorted(Comparator.comparing(Subfield::getCode))
      .toList();
  }

  private char getCode(Subfield subfield) {
    var subfieldModifications = linkingRuleDto.getSubfieldModifications();
    if (subfieldModifications != null && !subfieldModifications.isEmpty()) {
      for (SubfieldModification subfieldModification : subfieldModifications) {
        if (subfieldModification.getSource().charAt(0) == subfield.getCode()) {
          return subfieldModification.getTarget().charAt(0);
        }
      }
    }
    return subfield.getCode();
  }
}
