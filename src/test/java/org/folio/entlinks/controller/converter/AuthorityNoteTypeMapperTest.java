package org.folio.entlinks.controller.converter;

import org.folio.entlinks.domain.dto.AuthorityNoteTypeDto;
import org.folio.entlinks.domain.dto.AuthorityNoteTypeDtoCollection;
import org.folio.entlinks.domain.entity.AuthorityNoteType;
import org.folio.spring.test.type.UnitTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.entlinks.utils.DateUtils.fromTimestamp;
import static org.folio.support.base.TestConstants.*;

@UnitTest
public class AuthorityNoteTypeMapperTest {

  public static final String UPDATED_NAME = "NewName";
  private final AuthorityNoteTypeMapper mapper = new AuthorityNoteTypeMapperImpl();

  @Test
  public void testToEntity() {
    var dto = new AuthorityNoteTypeDto();
    dto.setId(TEST_ID);
    dto.setName(TEST_PROPERTY_VALUE);
    dto.setSource(TEST_PROPERTY_VALUE);

    AuthorityNoteType entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(dto.getId()).isEqualTo(entity.getId());
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(dto.getSource()).isEqualTo(entity.getSource());
  }

  @Test
  public void testToEntityWithNullDto() {
    AuthorityNoteType entity = mapper.toEntity(null);

    assertThat(entity).isNull();
  }

  @Test
  public void testToDto() {
    AuthorityNoteType entity = createAuthorityNoteType();

    AuthorityNoteTypeDto dto = mapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThat(entity.getId()).isEqualTo(dto.getId());
    assertThat(entity.getName()).isEqualTo(dto.getName());
    assertThat(entity.getSource()).isEqualTo(dto.getSource());
    assertThat(fromTimestamp(entity.getCreatedDate())).isEqualTo(dto.getMetadata().getCreatedDate());
    assertThat(entity.getCreatedByUserId()).isEqualTo(dto.getMetadata().getCreatedByUserId());
    assertThat(fromTimestamp(entity.getUpdatedDate())).isEqualTo(dto.getMetadata().getUpdatedDate());
    assertThat(entity.getUpdatedByUserId()).isEqualTo(dto.getMetadata().getUpdatedByUserId());
  }


  @Test
  public void testToDtoWithNullEntity() {
    AuthorityNoteTypeDto dto = mapper.toDto(null);

    assertThat(dto).isNull();
  }

  @Test
  public void testPartialUpdate() {
    var entity = createAuthorityNoteType();

    AuthorityNoteTypeDto dto = new AuthorityNoteTypeDto();
    dto.setName(UPDATED_NAME);

    AuthorityNoteType updatedEntity = mapper.partialUpdate(dto, entity);

    assertThat(dto).isNotNull();
    assertThat(entity.getId()).isEqualTo(updatedEntity.getId());
    assertThat(dto.getName()).isEqualTo(updatedEntity.getName());
    assertThat(entity.getSource()).isEqualTo(updatedEntity.getSource());
  }

  @Test
  public void testPartialUpdateWithNullDto() {
    AuthorityNoteType existingEntity = createAuthorityNoteType();

    AuthorityNoteType updatedEntity = mapper.partialUpdate(null, existingEntity);

    assertThat(updatedEntity).isNotNull();
    assertThat(existingEntity.getId()).isEqualTo(updatedEntity.getId());
    assertThat(existingEntity.getName()).isEqualTo(updatedEntity.getName());
    assertThat(existingEntity.getSource()).isEqualTo(updatedEntity.getSource());
  }

  @Test
  public void testToDtoList() {
    var entity = createAuthorityNoteType();

    var entityList = List.of(entity);
    List<AuthorityNoteTypeDto> dtoList = mapper.toDtoList(entityList);

    assertThat(dtoList).isNotNull();
    assertThat(1).isEqualTo(dtoList.size());

    AuthorityNoteTypeDto dto = dtoList.get(0);
    assertThat(dto.getId()).isEqualTo(entity.getId());
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(dto.getSource()).isEqualTo(entity.getSource());
    assertThat(fromTimestamp(entity.getCreatedDate())).isEqualTo(dto.getMetadata().getCreatedDate());
    assertThat(entity.getCreatedByUserId()).isEqualTo(dto.getMetadata().getCreatedByUserId());
    assertThat(fromTimestamp(entity.getUpdatedDate())).isEqualTo(dto.getMetadata().getUpdatedDate());
    assertThat(entity.getUpdatedByUserId()).isEqualTo(dto.getMetadata().getUpdatedByUserId());
  }

  @Test
  public void testToDtoListWithNullIterable() {
    List<AuthorityNoteTypeDto> dtoList = mapper.toDtoList(null);

    assertThat(dtoList).isNull();
  }


  @Test
  void testToAuthorityNoteTypeCollection() {
    List<AuthorityNoteType> noteTypesList = List.of(createAuthorityNoteType());
    Page<AuthorityNoteType> noteTypesPage = new PageImpl<>(noteTypesList);

    AuthorityNoteTypeDtoCollection dtoCollection = mapper.toAuthorityNoteTypeCollection(noteTypesPage);

    AuthorityNoteTypeDto noteTypeDto = dtoCollection.getAuthorityNoteTypes().get(0);
    AuthorityNoteType noteType = noteTypesList.get(0);
    assertThat(dtoCollection).isNotNull();
    assertThat(noteTypesList.size()).isEqualTo(dtoCollection.getTotalRecords());
    assertThat(noteType.getId()).isEqualTo(noteTypeDto.getId());
    assertThat(noteType.getName()).isEqualTo(noteTypeDto.getName());
    assertThat(fromTimestamp(noteType.getCreatedDate())).isEqualTo(noteTypeDto.getMetadata().getCreatedDate());
    assertThat(noteType.getCreatedByUserId()).isEqualTo(noteTypeDto.getMetadata().getCreatedByUserId());
    assertThat(fromTimestamp(noteType.getUpdatedDate())).isEqualTo(noteTypeDto.getMetadata().getUpdatedDate());
    assertThat(noteType.getUpdatedByUserId()).isEqualTo(noteTypeDto.getMetadata().getUpdatedByUserId());
  }

  @NotNull
  private static AuthorityNoteType createAuthorityNoteType() {
    var entity = new AuthorityNoteType();
    entity.setId(TEST_ID);
    entity.setName(TEST_PROPERTY_VALUE);
    entity.setSource(TEST_PROPERTY_VALUE);
    entity.setCreatedDate(TEST_DATE);
    entity.setCreatedByUserId(TEST_ID);
    entity.setUpdatedDate(TEST_DATE);
    entity.setUpdatedByUserId(TEST_ID);
    return entity;
  }
}
