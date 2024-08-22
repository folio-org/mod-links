package org.folio.entlinks.controller.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.folio.entlinks.domain.entity.AuthorityConstants.BROADER_TERM;
import static org.folio.entlinks.domain.entity.AuthorityConstants.CORPORATE_NAME_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.CORPORATE_NAME_TITLE_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.EARLIER_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.GENRE_TERM_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.GEOGRAPHIC_NAME_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.LATER_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.MEETING_NAME_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.MEETING_NAME_TITLE_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.NARROWER_TERM;
import static org.folio.entlinks.domain.entity.AuthorityConstants.PERSONAL_NAME_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.PERSONAL_NAME_TITLE_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.SAFT_TERM;
import static org.folio.entlinks.domain.entity.AuthorityConstants.SFT_TERM;
import static org.folio.entlinks.domain.entity.AuthorityConstants.TOPICAL_TERM_HEADING;
import static org.folio.entlinks.domain.entity.AuthorityConstants.UNIFORM_TITLE_HEADING;
import static org.folio.support.base.TestConstants.TEST_STRING;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.domain.entity.HeadingRef;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class AuthorityUtilityMapperTest {

  private final AuthorityDto source = new AuthorityDto();
  private final Authority target = new Authority();

  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthorityHeadingWithNonNullValues(String propertyType, String propertyValue) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> source.setPersonalName(propertyValue);
      case PERSONAL_NAME_TITLE_HEADING -> source.setPersonalNameTitle(propertyValue);
      case CORPORATE_NAME_HEADING -> source.setCorporateName(propertyValue);
      case CORPORATE_NAME_TITLE_HEADING -> source.setCorporateNameTitle(propertyValue);
      case MEETING_NAME_HEADING -> source.setMeetingName(propertyValue);
      case MEETING_NAME_TITLE_HEADING -> source.setMeetingNameTitle(propertyValue);
      case UNIFORM_TITLE_HEADING -> source.setUniformTitle(propertyValue);
      case TOPICAL_TERM_HEADING -> source.setTopicalTerm(propertyValue);
      case GEOGRAPHIC_NAME_HEADING -> source.setGeographicName(propertyValue);
      case GENRE_TERM_HEADING -> source.setGenreTerm(propertyValue);
      default -> fail("Invalid heading type - {} cannot be mapped", propertyType);
    }

    AuthorityUtilityMapper.extractAuthorityHeading(source, target);

    assertThat(propertyValue).isEqualTo(target.getHeading());
    assertThat(propertyType).isEqualTo(target.getHeadingType());
  }

  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthoritySftHeadingsWithNonNullValues(String propertyType, String propertyValue) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> source.setSftPersonalName(Collections.singletonList(propertyValue));
      case PERSONAL_NAME_TITLE_HEADING -> source.setSftPersonalNameTitle(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_HEADING -> source.setSftCorporateName(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_TITLE_HEADING -> source.setSftCorporateNameTitle(Collections.singletonList(propertyValue));
      case MEETING_NAME_HEADING -> source.setSftMeetingName(Collections.singletonList(propertyValue));
      case MEETING_NAME_TITLE_HEADING -> source.setSftMeetingNameTitle(Collections.singletonList(propertyValue));
      case UNIFORM_TITLE_HEADING -> source.setSftUniformTitle(Collections.singletonList(propertyValue));
      case TOPICAL_TERM_HEADING -> source.setSftTopicalTerm(Collections.singletonList(propertyValue));
      case GEOGRAPHIC_NAME_HEADING -> source.setSftGeographicName(Collections.singletonList(propertyValue));
      case GENRE_TERM_HEADING -> source.setSftGenreTerm(Collections.singletonList(propertyValue));
      default -> fail("Invalid sft heading type - {} cannot be mapped", propertyType);
    }

    AuthorityUtilityMapper.extractAuthoritySftHeadings(source, target);

    List<HeadingRef> sftHeadings = target.getSftHeadings();
    assertThat(sftHeadings).hasSize(1);
    assertThat(propertyValue).isEqualTo(sftHeadings.get(0).getHeading());
    assertThat(propertyType).isEqualTo(sftHeadings.get(0).getHeadingType());
  }

  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthoritySaftHeadingsWithNonNullValues(String propertyType, String propertyValue) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> source.setSaftPersonalName(Collections.singletonList(propertyValue));
      case PERSONAL_NAME_TITLE_HEADING -> source.setSaftPersonalNameTitle(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_HEADING -> source.setSaftCorporateName(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_TITLE_HEADING -> source.setSaftCorporateNameTitle(Collections.singletonList(propertyValue));
      case MEETING_NAME_HEADING -> source.setSaftMeetingName(Collections.singletonList(propertyValue));
      case MEETING_NAME_TITLE_HEADING -> source.setSaftMeetingNameTitle(Collections.singletonList(propertyValue));
      case UNIFORM_TITLE_HEADING -> source.setSaftUniformTitle(Collections.singletonList(propertyValue));
      case TOPICAL_TERM_HEADING -> source.setSaftTopicalTerm(Collections.singletonList(propertyValue));
      case GEOGRAPHIC_NAME_HEADING -> source.setSaftGeographicName(Collections.singletonList(propertyValue));
      case GENRE_TERM_HEADING -> source.setSaftGenreTerm(Collections.singletonList(propertyValue));
      default -> fail("Invalid saft heading type - {} cannot be mapped", propertyType);
    }

    AuthorityUtilityMapper.extractAuthoritySaftHeadings(source, target);

    List<HeadingRef> saftHeadings = target.getSaftHeadings();
    assertThat(saftHeadings).hasSize(1);
    assertThat(propertyValue).isEqualTo(saftHeadings.get(0).getHeading());
    assertThat(propertyType).isEqualTo(saftHeadings.get(0).getHeadingType());
  }

  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthorityDtoSftHeadings(String headingType, String headingValue) {

    List<HeadingRef> sftHeadings = new ArrayList<>();
    sftHeadings.add(new HeadingRef(headingType, headingValue));
    target.setSftHeadings(sftHeadings);

    AuthorityUtilityMapper.extractAuthorityDtoSftHeadings(target, source);

    switch (headingType) {
      case PERSONAL_NAME_HEADING -> assertTrue(source.getSftPersonalName().contains(headingValue));
      case PERSONAL_NAME_TITLE_HEADING -> assertTrue(source.getSftPersonalNameTitle().contains(headingValue));
      case CORPORATE_NAME_HEADING -> assertTrue(source.getSftCorporateName().contains(headingValue));
      case CORPORATE_NAME_TITLE_HEADING -> assertTrue(source.getSftCorporateNameTitle().contains(headingValue));
      case MEETING_NAME_HEADING -> assertTrue(source.getSftMeetingName().contains(headingValue));
      case MEETING_NAME_TITLE_HEADING -> assertTrue(source.getSftMeetingNameTitle().contains(headingValue));
      case UNIFORM_TITLE_HEADING -> assertTrue(source.getSftUniformTitle().contains(headingValue));
      case TOPICAL_TERM_HEADING -> assertTrue(source.getSftTopicalTerm().contains(headingValue));
      case GEOGRAPHIC_NAME_HEADING -> assertTrue(source.getSftGeographicName().contains(headingValue));
      case GENRE_TERM_HEADING -> assertTrue(source.getSftGenreTerm().contains(headingValue));
      default -> fail("Invalid sft heading type - {} cannot be mapped", headingType);
    }
  }

  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthorityDtoSaftHeadings(String headingType, String headingValue) {

    List<HeadingRef> saftHeadings = new ArrayList<>();
    saftHeadings.add(new HeadingRef(headingType, headingValue));
    target.setSaftHeadings(saftHeadings);

    AuthorityUtilityMapper.extractAuthorityDtoSaftHeadings(target, source);

    switch (headingType) {
      case PERSONAL_NAME_HEADING -> assertTrue(source.getSaftPersonalName().contains(headingValue));
      case PERSONAL_NAME_TITLE_HEADING -> assertTrue(source.getSaftPersonalNameTitle().contains(headingValue));
      case CORPORATE_NAME_HEADING -> assertTrue(source.getSaftCorporateName().contains(headingValue));
      case CORPORATE_NAME_TITLE_HEADING -> assertTrue(source.getSaftCorporateNameTitle().contains(headingValue));
      case MEETING_NAME_HEADING -> assertTrue(source.getSaftMeetingName().contains(headingValue));
      case MEETING_NAME_TITLE_HEADING -> assertTrue(source.getSaftMeetingNameTitle().contains(headingValue));
      case UNIFORM_TITLE_HEADING -> assertTrue(source.getSaftUniformTitle().contains(headingValue));
      case TOPICAL_TERM_HEADING -> assertTrue(source.getSaftTopicalTerm().contains(headingValue));
      case GEOGRAPHIC_NAME_HEADING -> assertTrue(source.getSaftGeographicName().contains(headingValue));
      case GENRE_TERM_HEADING -> assertTrue(source.getSaftGenreTerm().contains(headingValue));
      default -> fail("Invalid saft heading type - {} cannot be mapped", headingType);
    }
  }


  @ParameterizedTest
  @MethodSource("headingTypeAndValueProvider")
  void testExtractAuthorityDtoHeadingValue(String headingType, String headingValue) {
    target.setHeading(headingValue);
    target.setHeadingType(headingType);

    AuthorityUtilityMapper.extractAuthorityDtoHeadingValue(target, source);

    switch (headingType) {
      case PERSONAL_NAME_HEADING -> assertThat(source.getPersonalName()).isEqualTo(headingValue);
      case PERSONAL_NAME_TITLE_HEADING -> assertThat(source.getPersonalNameTitle()).isEqualTo(headingValue);
      case CORPORATE_NAME_HEADING -> assertThat(source.getCorporateName()).isEqualTo(headingValue);
      case CORPORATE_NAME_TITLE_HEADING -> assertThat(source.getCorporateNameTitle()).isEqualTo(headingValue);
      case MEETING_NAME_HEADING -> assertThat(source.getMeetingName()).isEqualTo(headingValue);
      case MEETING_NAME_TITLE_HEADING -> assertThat(source.getMeetingNameTitle()).isEqualTo(headingValue);
      case UNIFORM_TITLE_HEADING -> assertThat(source.getUniformTitle()).isEqualTo(headingValue);
      case TOPICAL_TERM_HEADING -> assertThat(source.getTopicalTerm()).isEqualTo(headingValue);
      case GEOGRAPHIC_NAME_HEADING -> assertThat(source.getGeographicName()).isEqualTo(headingValue);
      case GENRE_TERM_HEADING -> assertThat(source.getGenreTerm()).isEqualTo(headingValue);
      default -> fail("Invalid heading type - {} cannot be mapped", headingType);
    }
  }

  @ParameterizedTest
  @MethodSource("additionalHeadingTypeAndValuesProvider")
  void testExtractAuthorityAdditionalHeadingsWithNonNullValues(String propertyType, List<String> propertyValues) {
    switch (propertyType) {
      case BROADER_TERM -> source.setBroaderTerm(propertyValues);
      case NARROWER_TERM -> source.setNarrowerTerm(propertyValues);
      case EARLIER_HEADING -> source.setEarlierHeading(propertyValues);
      case LATER_HEADING -> source.setLaterHeading(propertyValues);
      case SAFT_TERM -> source.setSaftTerm(propertyValues);
      case SFT_TERM -> source.setSftTerm(propertyValues);
      default -> fail("Invalid heading type - {} cannot be mapped", propertyType);
    }

    AuthorityUtilityMapper.extractAuthorityAdditionalHeadings(source, target);

    List<HeadingRef> additionalHeadings = target.getAdditionalHeadings();
    String[] targetHeadingValues = additionalHeadings.stream().map(HeadingRef::getHeading).toArray(String[]::new);
    assertThat(additionalHeadings).hasSize(propertyValues.size());
    additionalHeadings.forEach(a -> assertEquals(propertyType, a.getHeadingType()));
    assertArrayEquals(propertyValues.toArray(), targetHeadingValues);
  }

  @ParameterizedTest
  @MethodSource("additionalHeadingTypeAndValuesProvider")
  void testExtractAuthorityAdditionalHeadingsWithNullValues(String propertyType, List<String> propertyValues) {
    switch (propertyType) {
      case BROADER_TERM -> source.setBroaderTerm(null);
      case NARROWER_TERM -> source.setNarrowerTerm(null);
      case EARLIER_HEADING -> source.setEarlierHeading(null);
      case LATER_HEADING -> source.setLaterHeading(null);
      case SAFT_TERM -> source.setSaftTerm(null);
      case SFT_TERM -> source.setSftTerm(null);
      default -> fail("Invalid heading type - {} cannot be mapped", propertyType);
    }

    AuthorityUtilityMapper.extractAuthorityAdditionalHeadings(source, target);

    assertThat(target.getAdditionalHeadings()).isEmpty();
  }

  @Test
  void testExtractAuthorityAdditionalHeadingsWithMixedHeadingTypes() {
    source.setBroaderTerm(List.of("boarderTerm1", "boarderTerm2"));
    source.setNarrowerTerm(List.of("narrowerTerm"));
    source.setEarlierHeading(List.of("earlierHeading"));
    source.setLaterHeading(List.of("laterHeading"));
    source.setSftTerm(List.of("sftTerm1", "sftTerm2", "sftTerm3"));
    source.setSaftTerm(List.of("saftTerm1", "saftTerm2"));

    AuthorityUtilityMapper.extractAuthorityAdditionalHeadings(source, target);

    List<HeadingRef> additionalHeadings = target.getAdditionalHeadings();
    String[] targetHeadingTypes = additionalHeadings.stream().map(HeadingRef::getHeadingType).toArray(String[]::new);
    String[] targetHeadingValues = additionalHeadings.stream().map(HeadingRef::getHeading).toArray(String[]::new);
    assertThat(additionalHeadings).hasSize(10);
    assertArrayEquals(new String[]{BROADER_TERM, BROADER_TERM, NARROWER_TERM, EARLIER_HEADING, LATER_HEADING, SFT_TERM,
      SFT_TERM, SFT_TERM, SAFT_TERM, SAFT_TERM}, targetHeadingTypes);
    assertArrayEquals(new String[]{"boarderTerm1", "boarderTerm2", "narrowerTerm", "earlierHeading", "laterHeading",
      "sftTerm1", "sftTerm2", "sftTerm3", "saftTerm1", "saftTerm2"}, targetHeadingValues);
  }

  @ParameterizedTest
  @MethodSource("additionalHeadingTypeAndValuesProvider")
  void testExtractAuthorityDtoAdditionalHeadingsWithNonNullValues(String headingType, List<String> headingValues) {
    List<HeadingRef> additionalHeadings = headingValues.stream().map(hv -> new HeadingRef(headingType, hv)).toList();
    target.setAdditionalHeadings(additionalHeadings);

    AuthorityUtilityMapper.extractAuthorityDtoAdditionalHeadings(target, source);

    switch (headingType) {
      case BROADER_TERM -> assertArrayEquals(source.getBroaderTerm().toArray(), headingValues.toArray());
      case NARROWER_TERM -> assertArrayEquals(source.getNarrowerTerm().toArray(), headingValues.toArray());
      case EARLIER_HEADING -> assertArrayEquals(source.getEarlierHeading().toArray(), headingValues.toArray());
      case LATER_HEADING -> assertArrayEquals(source.getLaterHeading().toArray(), headingValues.toArray());
      case SFT_TERM -> assertArrayEquals(source.getSftTerm().toArray(), headingValues.toArray());
      case SAFT_TERM -> assertArrayEquals(source.getSaftTerm().toArray(), headingValues.toArray());
      default -> fail("Invalid saft heading type - {} cannot be mapped", headingType);
    }
  }

  @Test
  void testExtractAuthorityDtoAdditionalHeadingsWithNullValues() {

    AuthorityUtilityMapper.extractAuthorityDtoAdditionalHeadings(target, source);

    assertTrue(source.getBroaderTerm().isEmpty());
    assertTrue(source.getNarrowerTerm().isEmpty());
    assertTrue(source.getEarlierHeading().isEmpty());
    assertTrue(source.getLaterHeading().isEmpty());
    assertTrue(source.getSftTerm().isEmpty());
    assertTrue(source.getSaftTerm().isEmpty());
  }

  @Test
  void testExtractAuthorityDtoAdditionalHeadingsWithMixedHeadingTypes() {
    List<HeadingRef> additionalHeadings = new ArrayList<>();
    additionalHeadings.add(new HeadingRef(BROADER_TERM, "broaderTerm"));
    additionalHeadings.add(new HeadingRef(NARROWER_TERM, "narrowerTerm1"));
    additionalHeadings.add(new HeadingRef(NARROWER_TERM, "narrowerTerm2"));
    additionalHeadings.add(new HeadingRef(EARLIER_HEADING, "earlierHeading1"));
    additionalHeadings.add(new HeadingRef(EARLIER_HEADING, "earlierHeading2"));
    additionalHeadings.add(new HeadingRef(LATER_HEADING, "laterHeading"));
    additionalHeadings.add(new HeadingRef(SFT_TERM, "sftTerm1"));
    additionalHeadings.add(new HeadingRef(SFT_TERM, "sftTerm2"));
    additionalHeadings.add(new HeadingRef(SAFT_TERM, "saftTerm1"));
    additionalHeadings.add(new HeadingRef(SAFT_TERM, "saftTerm2"));
    target.setAdditionalHeadings(additionalHeadings);

    AuthorityUtilityMapper.extractAuthorityDtoAdditionalHeadings(target, source);

    assertArrayEquals(new String[] {"broaderTerm"}, source.getBroaderTerm().toArray());
    assertArrayEquals(new String[] {"narrowerTerm1", "narrowerTerm2"}, source.getNarrowerTerm().toArray());
    assertArrayEquals(new String[] {"earlierHeading1", "earlierHeading2"}, source.getEarlierHeading().toArray());
    assertArrayEquals(new String[] {"laterHeading"}, source.getLaterHeading().toArray());
    assertArrayEquals(new String[] {"sftTerm1", "sftTerm2"}, source.getSftTerm().toArray());
    assertArrayEquals(new String[] {"saftTerm1", "saftTerm2"}, source.getSaftTerm().toArray());
  }

  private static Stream<Arguments> headingTypeAndValueProvider() {
    return Stream.of(
        arguments(PERSONAL_NAME_HEADING, TEST_STRING),
        arguments(PERSONAL_NAME_TITLE_HEADING, TEST_STRING),
        arguments(CORPORATE_NAME_HEADING, TEST_STRING),
        arguments(CORPORATE_NAME_TITLE_HEADING, TEST_STRING),
        arguments(MEETING_NAME_HEADING, TEST_STRING),
        arguments(MEETING_NAME_TITLE_HEADING, TEST_STRING),
        arguments(UNIFORM_TITLE_HEADING, TEST_STRING),
        arguments(TOPICAL_TERM_HEADING, TEST_STRING),
        arguments(GEOGRAPHIC_NAME_HEADING, TEST_STRING),
        arguments(GENRE_TERM_HEADING, TEST_STRING)
    );
  }

  private static Stream<Arguments> additionalHeadingTypeAndValuesProvider() {
    return Stream.of(
        arguments(BROADER_TERM, List.of(TEST_STRING)),
        arguments(NARROWER_TERM,  List.of(TEST_STRING, TEST_STRING)),
        arguments(EARLIER_HEADING,  List.of(TEST_STRING)),
        arguments(LATER_HEADING, List.of(TEST_STRING, TEST_STRING)),
        arguments(SFT_TERM, List.of(TEST_STRING, TEST_STRING)),
        arguments(SAFT_TERM, List.of(TEST_STRING, TEST_STRING, TEST_STRING)),
        arguments(SAFT_TERM, List.of())
    );
  }
}
