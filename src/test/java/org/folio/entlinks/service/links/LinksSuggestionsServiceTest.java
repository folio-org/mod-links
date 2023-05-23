package org.folio.entlinks.service.links;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.entlinks.domain.dto.FieldContent;
import org.folio.entlinks.domain.dto.LinkDetails;
import org.folio.entlinks.domain.dto.LinkStatus;
import org.folio.entlinks.domain.dto.ParsedRecordContent;
import org.folio.entlinks.domain.dto.ParsedRecordContentCollection;
import org.folio.entlinks.domain.dto.RecordType;
import org.folio.entlinks.domain.dto.StrippedParsedRecord;
import org.folio.entlinks.domain.dto.StrippedParsedRecordCollection;
import org.folio.entlinks.domain.dto.StrippedParsedRecordParsedRecord;
import org.folio.entlinks.domain.dto.SubfieldModification;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class LinksSuggestionsServiceTest {

  private static final String NO_SUGGESTIONS_ERROR_CODE = "101";
  private static final String MORE_THEN_ONE_SUGGESTIONS_ERROR_CODE = "102";
  private static final UUID AUTHORITY_ID = UUID.randomUUID();
  private static final String NATURAL_ID = "12345";

  private @Spy LinksSuggestionService linksSuggestionService;

  @Test
  void fillLinkDetailsWithSuggestedAuthorities_shouldFillLinkDetails_withNewLink() {
    var rules = getMapRule("100", "100");
    var bib = getBibParsedRecordContent("100", null);
    var authority = getStrippedAuthorityRecord("100");
    var strippedParsedRecords = new StrippedParsedRecordCollection(List.of(authority), 1);
    var parsedContentCollection = new ParsedRecordContentCollection().records(List.of(bib));

    linksSuggestionService
      .fillLinkDetailsWithSuggestedAuthorities(parsedContentCollection, strippedParsedRecords, rules);

    var bibField = bib.getFields().get("100");
    var linkDetails = bibField.getLinkDetails();
    assertEquals(linkDetails.getLinksStatus(), LinkStatus.NEW);
    assertEquals(linkDetails.getAuthorityId(), AUTHORITY_ID);
    //   assertEquals(linkDetails.getNaturalId(), NATURAL_ID);
    assertEquals(linkDetails.getRuleId(), 1);
    assertNull(linkDetails.getErrorStatusCode());

    var bibSubfields = bibField.getSubfields();
    assertTrue(bibSubfields.containsKey("a"));
    assertEquals(bibSubfields.get("9"), AUTHORITY_ID.toString());
    //  assertEquals(bibSubfields.get("0"), NATURAL_ID);
  }

  @Test
  void fillLinkDetailsWithSuggestedAuthorities_shouldFillLinkDetails_withActualLink() {
    var rules = getMapRule("100", "100");
    var bib = getBibParsedRecordContent("100", getActualLinksDetails());
    var authority = getStrippedAuthorityRecord("100");
    var strippedParsedRecords = new StrippedParsedRecordCollection(List.of(authority), 1);
    var parsedContentCollection = new ParsedRecordContentCollection().records(List.of(bib));

    linksSuggestionService
      .fillLinkDetailsWithSuggestedAuthorities(parsedContentCollection, strippedParsedRecords, rules);

    var bibField = bib.getFields().get("100");
    var linkDetails = bibField.getLinkDetails();
    assertEquals(linkDetails.getLinksStatus(), LinkStatus.ACTUAL);
    assertEquals(linkDetails.getAuthorityId(), AUTHORITY_ID);
    //   assertEquals(linkDetails.getNaturalId(), AUTHORITY_ID);
    assertEquals(linkDetails.getRuleId(), 1);
    assertNull(linkDetails.getErrorStatusCode());

    var bibSubfields = bibField.getSubfields();
    assertTrue(bibSubfields.containsKey("a"));
    assertEquals(bibSubfields.get("9"), AUTHORITY_ID.toString());
    //  assertEquals(bibSubfields.get("0"), NATURAL_ID);
  }

  @Test
  void fillLinkDetailsWithSuggestedAuthorities_shouldFillLinkDetails_withError_whenRequiredAuthoritySubfieldNotExist() {
    var rules = getMapRule("100", "100");
    var bib = getBibParsedRecordContent("100", null);
    var authority = getStrippedAuthorityRecord("100", emptyMap());
    var strippedParsedRecords = new StrippedParsedRecordCollection(List.of(authority), 1);
    var parsedContentCollection = new ParsedRecordContentCollection().records(List.of(bib));

    linksSuggestionService
      .fillLinkDetailsWithSuggestedAuthorities(parsedContentCollection, strippedParsedRecords, rules);

    var linkDetails = bib.getFields().get("100").getLinkDetails();
    assertEquals(linkDetails.getLinksStatus(), LinkStatus.ERROR);
    assertEquals(linkDetails.getErrorStatusCode(), NO_SUGGESTIONS_ERROR_CODE);
  }

  @Test
  void fillLinkDetailsWithSuggestedAuthorities_shouldFillLinkDetails_withErrorMoreThenOneAuthoritiesFound() {
    var rules = getMapRule("100", "100");
    var bib = getBibParsedRecordContent("100", getActualLinksDetails());
    var authority = getStrippedAuthorityRecord("100");
    var secondAuthority = getStrippedAuthorityRecord("100");
    var strippedParsedRecords = new StrippedParsedRecordCollection(List.of(authority, secondAuthority), 2);
    var parsedContentCollection = new ParsedRecordContentCollection().records(List.of(bib));

    linksSuggestionService
      .fillLinkDetailsWithSuggestedAuthorities(parsedContentCollection, strippedParsedRecords, rules);

    var linkDetails = bib.getFields().get("100").getLinkDetails();
    assertEquals(linkDetails.getLinksStatus(), LinkStatus.ERROR);
    assertEquals(linkDetails.getErrorStatusCode(), MORE_THEN_ONE_SUGGESTIONS_ERROR_CODE);
    assertNull(linkDetails.getAuthorityId());
    assertNull(linkDetails.getNaturalId());
    assertNull(linkDetails.getRuleId());
  }

  @Test
  void fillLinkDetailsWithSuggestedAuthorities_shouldFillLinkDetails_withErrorNoAuthoritiesFound() {
    var rules = getMapRule("100", "100");
    var bib = getBibParsedRecordContent("100", null);
    var authority = getStrippedAuthorityRecord("110");
    var strippedParsedRecords = new StrippedParsedRecordCollection(List.of(authority), 1);
    var parsedContentCollection = new ParsedRecordContentCollection().records(List.of(bib));

    linksSuggestionService
      .fillLinkDetailsWithSuggestedAuthorities(parsedContentCollection, strippedParsedRecords, rules);

    var linkDetails = bib.getFields().get("100").getLinkDetails();
    assertEquals(linkDetails.getLinksStatus(), LinkStatus.ERROR);
    assertEquals(linkDetails.getErrorStatusCode(), NO_SUGGESTIONS_ERROR_CODE);
  }

  private StrippedParsedRecord getStrippedAuthorityRecord(String authorityField) {
    return getStrippedAuthorityRecord(authorityField, Map.of("a", "test"));
  }

  private StrippedParsedRecord getStrippedAuthorityRecord(String authorityField, Map<String, String> subfields) {
    var field = new FieldContent().subfields(subfields);
    var fields = Map.of(authorityField, field);
    var strippedAuthorityContent = new ParsedRecordContent(fields, "default leader");
    var strippedAuthority = new StrippedParsedRecordParsedRecord(strippedAuthorityContent);
    return new StrippedParsedRecord(AUTHORITY_ID, RecordType.MARC_AUTHORITY, strippedAuthority);
  }

  private ParsedRecordContent getBibParsedRecordContent(String bibField, LinkDetails linkDetails) {
    var field = new FieldContent().linkDetails(linkDetails);
    var fields = Map.of(bibField, field);
    return new ParsedRecordContent(fields, "default leader");
  }

  private LinkDetails getActualLinksDetails() {
    return new LinkDetails().ruleId(2)
      .linksStatus(LinkStatus.ACTUAL)
      .authorityId(UUID.randomUUID())
      .naturalId(NATURAL_ID);
  }

  private Map<String, List<InstanceAuthorityLinkingRule>> getMapRule(String bibField, String authorityField) {
    var modification = new SubfieldModification().source("a").target("b");
    var existence = Map.of("a", true);

    var rule = new InstanceAuthorityLinkingRule();
    rule.setId(1);
    rule.setBibField(bibField);
    rule.setAuthorityField(authorityField);
    rule.setAutoLinkingEnabled(true);
    rule.setAuthoritySubfields(new char[] {'a', 'b'});
    rule.setSubfieldModifications(List.of(modification));
    rule.setSubfieldsExistenceValidations(existence);

    return Map.of(bibField, List.of(rule));
  }
}
