package org.folio.entlinks.integration.dto;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AuthorityParsedContentTest {

  @Test
  void getFieldByTag_WhenTagDoesNotExist_ExpectEmptyOptional() {
    var parsedContent = new AuthorityParsedContent(randomUUID(), "naturalId", "leader",
      List.of(new FieldParsedContent("tag1", "ind1", "ind2", emptyList(), null)), randomUUID());

    var actual = parsedContent.getFieldByTag("nonexistentTag");

    assertTrue(actual.isEmpty());
  }

  @Test
  void getFieldByTag_WhenTagExists_ExpectField() {
    var fieldTag = "tag1";
    var parsedContent = new FieldParsedContent(fieldTag, "ind1", "ind2", emptyList(), null);
    var authorityParsedContent = new AuthorityParsedContent(randomUUID(), "naturalId", "leader",
      List.of(parsedContent), randomUUID());

    var actual = authorityParsedContent.getFieldByTag(fieldTag);

    assertTrue(actual.isPresent());
    assertEquals(fieldTag, actual.get().getTag());
  }

  @Test
  void getFieldByTag_WhenTagIsNull_ExpectEmptyOptional() {
    var fieldTag = "tag1";
    var fieldParsedContent = new FieldParsedContent(fieldTag, "ind1", "ind2", emptyList(), null);
    var parsedContent = new AuthorityParsedContent(randomUUID(), "naturalId", "leader",
      List.of(fieldParsedContent), randomUUID());

    var actual = parsedContent.getFieldByTag(null);

    assertTrue(actual.isEmpty());
  }
}
