package org.folio.entlinks.integration.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class FieldParsedContentTest {

  private static final String TAG = "tag";
  private static final String IND_1 = "ind1";
  private static final String IND_2 = "ind2";

  private final ParsedSubfield subfieldA1 = new ParsedSubfield('a', "value1");
  private final ParsedSubfield subfieldA2 = new ParsedSubfield('a', "value2");
  private final ParsedSubfield subfieldB = new ParsedSubfield('b', "value3");
  private final ParsedSubfield subfield9 = new ParsedSubfield('9', "value1");
  private final ParsedSubfield subfield0 = new ParsedSubfield('0', "value1");

  @Test
  void testGetSubfields() {
    var subfields = List.of(subfieldA1, subfieldB, subfieldA2);
    var content = new FieldParsedContent(TAG, IND_1, IND_2, subfields, null);

    var actual = content.getSubfields('a');

    assertEquals(actual, List.of(subfieldA1, subfieldA2));
  }

  @Test
  void testGetSubfields_NoMatch() {
    var subfields = List.of(subfieldA1, subfieldB);
    var content = new FieldParsedContent(TAG, IND_1, IND_2, subfields, null);

    var actual = content.getSubfields('c');

    assertNull(actual);
  }

  @Test
  void testGetIdSubfields() {
    var subfields = List.of(subfieldB, subfield9);
    var content = new FieldParsedContent(TAG, IND_1, IND_2, subfields, null);

    var actual = content.getIdSubfields();

    assertEquals(List.of(subfield9), actual);
  }

  @Test
  void testGetNaturalIdSubfields() {
    var subfields = List.of(subfield0, subfieldB, subfield9);
    var content = new FieldParsedContent(TAG, IND_1, IND_2, subfields, null);

    var actual = content.getNaturalIdSubfields();

    assertEquals(actual, List.of(subfield0));
  }

  @Test
  void testHasSubfield() {
    var subfields = List.of(subfieldA1, subfieldA2, subfieldB, subfield9, subfield0);
    var content = new FieldParsedContent(TAG, IND_1, IND_2, subfields, null);

    assertTrue(content.hasSubfield('a'));
    assertFalse(content.hasSubfield('c'));
  }
}
