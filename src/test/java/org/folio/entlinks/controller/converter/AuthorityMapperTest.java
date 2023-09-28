package org.folio.entlinks.controller.converter;

import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.dto.AuthorityDtoIdentifier;
import org.folio.entlinks.domain.dto.AuthorityDtoNote;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.domain.entity.AuthorityIdentifier;
import org.folio.entlinks.domain.entity.AuthorityNote;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorityMapperTest {

  private static final String TEST_PROPERTY_VALUE = "test";
  private static final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  public static final int TEST_VERSION = 2;

  private final AuthorityMapper authorityMapper = new AuthorityMapperImpl();

  @Test
  void testToEntityWithValidDto() {
    var dto = new AuthorityDto();
    dto.setId(TEST_UUID);
    dto.setNaturalId(TEST_PROPERTY_VALUE);
    dto.setSource(TEST_PROPERTY_VALUE);
    dto.setVersion(TEST_VERSION);

    Authority authority = authorityMapper.toEntity(dto);

    assertThat(authority).isNotNull();
    assertThat(dto.getId()).isEqualTo(authority.getId());
    assertThat(dto.getNaturalId()).isEqualTo(authority.getNaturalId());
    assertThat(dto.getSource()).isEqualTo(authority.getSource());
    assertThat(dto.getVersion()).isEqualTo(authority.getVersion());
  }

  @Test
  void testToDtoWithNullInput() {
    Authority authority = authorityMapper.toEntity(null);

    assertThat(authority).isNull();
  }

  @Test
  void testToDtoWithValidData() {
    var authority = new Authority();
    authority.setId(TEST_UUID);
    authority.setVersion(TEST_VERSION);
    authority.setSource(TEST_PROPERTY_VALUE);
    authority.setNaturalId(TEST_PROPERTY_VALUE);

    AuthorityDto authorityDto = authorityMapper.toDto(authority);

    assertThat(authorityDto).isNotNull();
    assertThat(TEST_UUID).isEqualTo(authorityDto.getId());
    assertThat(TEST_VERSION).isEqualTo(authorityDto.getVersion());
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(authorityDto.getSource());
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(authorityDto.getNaturalId());
  }

  @Test
  void testToAuthorityWithNullInput() {
    AuthorityDto authorityDto = authorityMapper.toDto(null);

    assertThat(authorityDto).isNull();
  }

  @Test
  void testToAuthorityIdentifierWithValidData() {
    var dtoIdentifier = new AuthorityDtoIdentifier();
    dtoIdentifier.setValue(TEST_PROPERTY_VALUE);
    dtoIdentifier.setIdentifierTypeId(TEST_UUID);

    AuthorityIdentifier authorityIdentifier = authorityMapper.toAuthorityIdentifier(dtoIdentifier);

    assertThat(authorityIdentifier).isNotNull();
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(authorityIdentifier.getValue());
    assertThat(TEST_UUID).isEqualTo(authorityIdentifier.getIdentifierTypeId());

  }

  @Test
  void testToAuthorityIdentifierWithNullInput() {
    AuthorityIdentifier authorityIdentifier = authorityMapper.toAuthorityIdentifier(null);

    assertThat(authorityIdentifier).isNull();
  }


  @Test
  void testToAuthorityNoteWithValidData() {
    var dtoNote = new AuthorityDtoNote();
    dtoNote.setNoteTypeId(TEST_UUID);
    dtoNote.setNote(TEST_PROPERTY_VALUE);
    dtoNote.setStaffOnly(true);

    AuthorityNote authorityNote = authorityMapper.toAuthorityNote(dtoNote);

    assertThat(authorityNote).isNotNull();
    assertThat(TEST_UUID).isEqualTo(authorityNote.getNoteTypeId());
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(authorityNote.getNote());
    assertThat(authorityNote.getStaffOnly()).isTrue();
  }

  @Test
  void testToAuthorityNoteWithNullInput() {
    AuthorityNote authorityNote = authorityMapper.toAuthorityNote(null);

    assertThat(authorityNote).isNull();
  }

  @Test
  void testToAuthorityDtoNoteWithValidData() {
    var authorityNote = new AuthorityNote();
    authorityNote.setNoteTypeId(TEST_UUID);
    authorityNote.setNote(TEST_PROPERTY_VALUE);
    authorityNote.setStaffOnly(true);

    AuthorityDtoNote dtoNote = authorityMapper.toAuthorityDtoNote(authorityNote);

    assertThat(TEST_UUID).isEqualTo(dtoNote.getNoteTypeId());
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(dtoNote.getNote());
    assertThat(dtoNote.getStaffOnly()).isTrue();
  }

  @Test
  void testToAuthorityDtoNoteWithNullInput() {
    AuthorityDtoNote dtoNote = authorityMapper.toAuthorityDtoNote(null);

    assertThat(dtoNote).isNull();
  }

  @Test
  void testToDtoListWithValidData() {
    var authority1 = new Authority()
        .withId(TEST_UUID)
        .withVersion(TEST_VERSION)
        .withSource(TEST_PROPERTY_VALUE);
    var authority2 = new Authority()
        .withId(TEST_UUID)
        .withVersion(TEST_VERSION)
        .withSource(TEST_PROPERTY_VALUE);

    var authorityList = new ArrayList<>(Arrays.asList(authority1, authority2));

    List<AuthorityDto> dtoList = authorityMapper.toDtoList(authorityList);

    assertThat(2).isEqualTo(dtoList.size());
    AuthorityDto dto1 = dtoList.get(0);
    assertThat(TEST_UUID).isEqualTo(dto1.getId());
    assertThat(TEST_VERSION).isEqualTo(dto1.getVersion());
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(dto1.getSource());

  }

  @Test
  void testToDtoListWithNullInput() {
    List<AuthorityDto> dtoList = authorityMapper.toDtoList(null);

    assertThat(dtoList).isNull();
  }

  @Test
  void testToAuthorityDtoIdentifierWithValidData() {
    var authorityIdentifier = new AuthorityIdentifier();
    authorityIdentifier.setValue(TEST_PROPERTY_VALUE);
    authorityIdentifier.setIdentifierTypeId(TEST_UUID);

    AuthorityDtoIdentifier dtoIdentifier = authorityMapper.toAuthorityDtoIdentifier(authorityIdentifier);

    assertThat(dtoIdentifier).isNotNull();
    assertThat(TEST_PROPERTY_VALUE).isEqualTo(dtoIdentifier.getValue());
    assertThat(TEST_UUID).isEqualTo(dtoIdentifier.getIdentifierTypeId());
  }

  @Test
  void testToAuthorityDtoIdentifierWithNullInput() {
    AuthorityDtoIdentifier dtoIdentifier = authorityMapper.toAuthorityDtoIdentifier(null);

    assertThat(dtoIdentifier).isNull();
  }
}
