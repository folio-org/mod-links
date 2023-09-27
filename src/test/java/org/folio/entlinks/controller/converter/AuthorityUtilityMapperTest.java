package org.folio.entlinks.controller.converter;

import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.domain.entity.HeadingRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.entlinks.domain.entity.AuthorityConstants.*;
import static org.folio.support.base.TestConstants.INVALID_HEADING_TYPE;
import static org.folio.support.base.TestConstants.TEST_STRING;

public class AuthorityUtilityMapperTest {

  private final AuthorityDto target = new AuthorityDto();
  private final Authority source = new Authority();

  @ParameterizedTest
  @CsvSource({
      TEST_STRING + PERSONAL_NAME_HEADING,
      TEST_STRING + PERSONAL_NAME_TITLE_HEADING,
      TEST_STRING + CORPORATE_NAME_HEADING,
      TEST_STRING + CORPORATE_NAME_TITLE_HEADING,
      TEST_STRING + MEETING_NAME_HEADING,
      TEST_STRING + MEETING_NAME_TITLE_HEADING,
      TEST_STRING + UNIFORM_TITLE_HEADING,
      TEST_STRING + TOPICAL_TERM_HEADING,
      TEST_STRING + GEOGRAPHIC_NAME_HEADING,
      TEST_STRING + GENRE_TERM_HEADING
  })
  void testExtractAuthorityHeadingWithNonNullValues(String propertyValue, String propertyType) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> target.setPersonalName(propertyValue);
      case PERSONAL_NAME_TITLE_HEADING -> target.setPersonalNameTitle(propertyValue);
      case CORPORATE_NAME_HEADING -> target.setCorporateName(propertyValue);
      case CORPORATE_NAME_TITLE_HEADING -> target.setCorporateNameTitle(propertyValue);
      case MEETING_NAME_HEADING -> target.setMeetingName(propertyValue);
      case MEETING_NAME_TITLE_HEADING -> target.setMeetingNameTitle(propertyValue);
      case UNIFORM_TITLE_HEADING -> target.setUniformTitle(propertyValue);
      case TOPICAL_TERM_HEADING -> target.setTopicalTerm(propertyValue);
      case GEOGRAPHIC_NAME_HEADING -> target.setGeographicName(propertyValue);
      case GENRE_TERM_HEADING -> target.setGenreTerm(propertyValue);
    }

    AuthorityUtilityMapper.extractAuthorityHeading(target, source);

    assertThat(propertyValue).isEqualTo(source.getHeading());
    assertThat(propertyType).isEqualTo(source.getHeadingType());
  }

  @ParameterizedTest
  @CsvSource({
      TEST_STRING + PERSONAL_NAME_HEADING,
      TEST_STRING + PERSONAL_NAME_TITLE_HEADING,
      TEST_STRING + CORPORATE_NAME_HEADING,
      TEST_STRING + CORPORATE_NAME_TITLE_HEADING,
      TEST_STRING + MEETING_NAME_HEADING,
      TEST_STRING + MEETING_NAME_TITLE_HEADING,
      TEST_STRING + UNIFORM_TITLE_HEADING,
      TEST_STRING + TOPICAL_TERM_HEADING,
      TEST_STRING + GEOGRAPHIC_NAME_HEADING,
      TEST_STRING + GENRE_TERM_HEADING
  })
  void testExtractAuthoritySftHeadingsWithNonNullValues(String propertyValue, String propertyType) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> target.setSftPersonalName(Collections.singletonList(propertyValue));
      case PERSONAL_NAME_TITLE_HEADING -> target.setSftPersonalNameTitle(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_HEADING -> target.setSftCorporateName(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_TITLE_HEADING -> target.setSftCorporateNameTitle(Collections.singletonList(propertyValue));
      case MEETING_NAME_HEADING -> target.setSftMeetingName(Collections.singletonList(propertyValue));
      case MEETING_NAME_TITLE_HEADING -> target.setSftMeetingNameTitle(Collections.singletonList(propertyValue));
      case UNIFORM_TITLE_HEADING -> target.setSftUniformTitle(Collections.singletonList(propertyValue));
      case TOPICAL_TERM_HEADING -> target.setSftTopicalTerm(Collections.singletonList(propertyValue));
      case GEOGRAPHIC_NAME_HEADING -> target.setSftGeographicName(Collections.singletonList(propertyValue));
      case GENRE_TERM_HEADING -> target.setSftGenreTerm(Collections.singletonList(propertyValue));
    }

    AuthorityUtilityMapper.extractAuthoritySftHeadings(target, source);

    List<HeadingRef> sftHeadings = source.getSftHeadings();
    assertThat(1).isEqualTo(sftHeadings.size());
    assertThat(propertyValue).isEqualTo(sftHeadings.get(0).getHeading());
    assertThat(propertyType).isEqualTo(sftHeadings.get(0).getHeadingType());
  }

  @ParameterizedTest
  @CsvSource({
      TEST_STRING + PERSONAL_NAME_HEADING,
      TEST_STRING + PERSONAL_NAME_TITLE_HEADING,
      TEST_STRING + CORPORATE_NAME_HEADING,
      TEST_STRING + CORPORATE_NAME_TITLE_HEADING,
      TEST_STRING + MEETING_NAME_HEADING,
      TEST_STRING + MEETING_NAME_TITLE_HEADING,
      TEST_STRING + UNIFORM_TITLE_HEADING,
      TEST_STRING + TOPICAL_TERM_HEADING,
      TEST_STRING + GEOGRAPHIC_NAME_HEADING,
      TEST_STRING + GENRE_TERM_HEADING
  })
  void testExtractAuthoritySaftHeadingsWithNonNullValues(String propertyValue, String propertyType) {
    switch (propertyType) {
      case PERSONAL_NAME_HEADING -> target.setSaftPersonalName(Collections.singletonList(propertyValue));
      case PERSONAL_NAME_TITLE_HEADING -> target.setSaftPersonalNameTitle(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_HEADING -> target.setSaftCorporateName(Collections.singletonList(propertyValue));
      case CORPORATE_NAME_TITLE_HEADING -> target.setSaftCorporateNameTitle(Collections.singletonList(propertyValue));
      case MEETING_NAME_HEADING -> target.setSaftMeetingName(Collections.singletonList(propertyValue));
      case MEETING_NAME_TITLE_HEADING -> target.setSaftMeetingNameTitle(Collections.singletonList(propertyValue));
      case UNIFORM_TITLE_HEADING -> target.setSaftUniformTitle(Collections.singletonList(propertyValue));
      case TOPICAL_TERM_HEADING -> target.setSaftTopicalTerm(Collections.singletonList(propertyValue));
      case GEOGRAPHIC_NAME_HEADING -> target.setSaftGeographicName(Collections.singletonList(propertyValue));
      case GENRE_TERM_HEADING -> target.setSaftGenreTerm(Collections.singletonList(propertyValue));
    }

    AuthorityUtilityMapper.extractAuthoritySaftHeadings(target, source);

    List<HeadingRef> saftHeadings = source.getSaftHeadings();
    assertThat(1).isEqualTo(saftHeadings.size());
    assertThat(propertyValue).isEqualTo(saftHeadings.get(0).getHeading());
    assertThat(propertyType).isEqualTo(saftHeadings.get(0).getHeadingType());
  }


  @Test
  void testExtractAuthorityDtoHeadingValue() {
    source.setHeadingType(MEETING_NAME_TITLE_HEADING);
    source.setHeading(TEST_STRING);

    AuthorityUtilityMapper.extractAuthorityDtoHeadingValue(source, target);

    assertThat(TEST_STRING).isEqualTo(target.getMeetingNameTitle());
  }

  @Test
  void testExtractAuthorityDtoHeadingValueWithInvalidHeadingType() {
    source.setHeadingType(INVALID_HEADING_TYPE);
    source.setHeading(TEST_STRING);

    AuthorityUtilityMapper.extractAuthorityDtoHeadingValue(source, target);

    assertThat(target.getPersonalName()).isNull();
  }
}
