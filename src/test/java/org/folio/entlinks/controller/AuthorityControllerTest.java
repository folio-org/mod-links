package org.folio.entlinks.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.entlinks.controller.delegate.AuthorityArchiveServiceDelegate;
import org.folio.entlinks.controller.delegate.AuthorityServiceDelegate;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.dto.AuthorityDtoCollection;
import org.folio.entlinks.exception.RequestBodyValidationException;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthorityControllerTest {

  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final String TEXT_CONTENT_TYPE = "text/plain";
  private static final String CQL_QUERY = "(cql.allRecords=1)";

  private static AuthorityDto dto;

  @Mock
  private AuthorityServiceDelegate authorityServiceDelegate;

  @Mock
  private AuthorityArchiveServiceDelegate authorityArchiveServiceDelegate;

  @InjectMocks
  private AuthorityController controller;

  @BeforeAll
  static void setup() {
    dto = new AuthorityDto();
    dto.setId(UUID.randomUUID());
    dto.setSource("FOLIO");
    dto.setPersonalName("Personal Name");
    dto.setNaturalId("1234");
  }

  @Test
  void shouldRetrieveAuthorities() {
    var collectionDto = new AuthorityDtoCollection(List.of(dto), 1);
    when(authorityServiceDelegate.retrieveAuthorityCollection(anyInt(), anyInt(), anyString(), anyBoolean()))
        .thenReturn(collectionDto);

    var response = controller.retrieveAuthorities(false, false, 0, 10, CQL_QUERY, JSON_CONTENT_TYPE);

    assertThat(response).isEqualTo(ResponseEntity.ok(collectionDto));
    verifyNoInteractions(authorityArchiveServiceDelegate);
  }

  @Test
  void shouldRetrieveAuthoritiesIds() {
    var collectionDto = new AuthorityDtoCollection(List.of(dto, dto), 1);
    when(authorityServiceDelegate.retrieveAuthorityCollection(anyInt(), anyInt(), anyString(), anyBoolean()))
        .thenReturn(collectionDto);

    var response = controller.retrieveAuthorities(false, true, 0, 10, CQL_QUERY, TEXT_CONTENT_TYPE);

    assertThat(response.getBody()).isEqualTo(dto.getId().toString() + "\n" + dto.getId().toString());
    verifyNoInteractions(authorityArchiveServiceDelegate);
  }

  @Test
  void shouldRetrieveAuthorityArchives() {
    var collectionDto = new AuthorityDtoCollection(List.of(dto), 1);
    when(authorityArchiveServiceDelegate.retrieveAuthorityArchives(anyInt(), anyInt(), anyString(), anyBoolean()))
        .thenReturn(collectionDto);

    var response = controller.retrieveAuthorities(true, false, 0, 10, CQL_QUERY, JSON_CONTENT_TYPE);

    assertThat(response).isEqualTo(ResponseEntity.ok(collectionDto));
    verifyNoInteractions(authorityServiceDelegate);
  }

  @Test
  void shouldRetrieveAuthorityArchivesIds() {
    var collectionDto = new AuthorityDtoCollection(List.of(dto, dto), 1);
    when(authorityArchiveServiceDelegate.retrieveAuthorityArchives(anyInt(), anyInt(), anyString(), anyBoolean()))
        .thenReturn(collectionDto);

    var response = controller.retrieveAuthorities(true, true, 0, 10, CQL_QUERY, TEXT_CONTENT_TYPE);

    assertThat(response.getBody()).isEqualTo(dto.getId().toString() + "\n" + dto.getId().toString());
    verifyNoInteractions(authorityServiceDelegate);
  }

  @Test
  void shouldThrowExceptionWhenPlainTextContentRequestedForAuthorities() {
    var collectionDto = new AuthorityDtoCollection(List.of(dto), 1);
    when(authorityServiceDelegate.retrieveAuthorityCollection(anyInt(), anyInt(), anyString(), anyBoolean()))
        .thenReturn(collectionDto);

    assertThrows(RequestBodyValidationException.class, () ->
        controller.retrieveAuthorities(false, false, 0, 10, CQL_QUERY, TEXT_CONTENT_TYPE));
  }
}
