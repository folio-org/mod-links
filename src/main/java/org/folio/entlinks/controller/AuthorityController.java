package org.folio.entlinks.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.folio.entlinks.controller.delegate.AuthorityArchiveServiceDelegate;
import org.folio.entlinks.controller.delegate.AuthorityServiceDelegate;
import org.folio.entlinks.domain.dto.AuthorityDto;
import org.folio.entlinks.domain.dto.AuthorityDtoCollection;
import org.folio.entlinks.exception.RequestBodyValidationException;
import org.folio.entlinks.rest.resource.AuthorityStorageApi;
import org.folio.tenant.domain.dto.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
public class AuthorityController implements AuthorityStorageApi {

  public static final String RETRIEVE_COLLECTION_DTO_ERROR_MESSAGE =
      "It is not allowed to retrieve authorities in text/plain format unless only retrieving of authorities' IDs "
          + "is requested";

  private final AuthorityServiceDelegate delegate;
  private final AuthorityArchiveServiceDelegate authorityArchiveServiceDelegate;

  @Override
  public ResponseEntity<AuthorityDto> createAuthority(AuthorityDto authority) {
    var created = delegate.createAuthority(authority);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Override
  public ResponseEntity<Void> deleteAuthority(UUID id) {
    delegate.deleteAuthorityById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<AuthorityDto> getAuthority(UUID id) {
    var authority = delegate.getAuthorityById(id);
    return ResponseEntity.ok(authority);
  }

  @Override
  public ResponseEntity retrieveAuthorities(Boolean deleted, Boolean idOnly, Integer offset, Integer limit,
                                            String query, @RequestHeader("Content-type") String contentType) {
    var collectionDto = Boolean.TRUE.equals(deleted)
        ? authorityArchiveServiceDelegate.retrieveAuthorityArchives(offset, limit, query, idOnly)
        : delegate.retrieveAuthorityCollection(offset, limit, query, idOnly);

    return getAuthoritiesCollectionResponse(collectionDto, contentType, idOnly);
  }

  @Override
  public ResponseEntity<Void> updateAuthority(UUID id, AuthorityDto authority) {
    delegate.updateAuthority(id, authority);
    return ResponseEntity.noContent().build();
  }

  /**
   * POST /authority-storage/expire/authorities.
   *
   * @return Successfully published authorities expire job (status code 202)
   *         or Internal server error. (status code 500)
   */
  @PostMapping(
      value = "/authority-storage/expire/authorities",
      produces = { "application/json" }
  )
  public ResponseEntity<Void> expireAuthorities() {
    authorityArchiveServiceDelegate.expire();
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private ResponseEntity<Object> getAuthoritiesCollectionResponse(AuthorityDtoCollection collectionDto,
                                                                  String contentType,
                                                                  Boolean idOnly) {
    if (contentType != null && contentType.startsWith(MediaType.TEXT_PLAIN_VALUE)) {
      if (Boolean.FALSE.equals(idOnly)) {
        throw new RequestBodyValidationException(RETRIEVE_COLLECTION_DTO_ERROR_MESSAGE,
            List.of(new Parameter("Content-type").value(contentType), new Parameter("idOnly").value("false")));
      }

      var headers = new HttpHeaders();
      headers.setContentType(MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE));
      return new ResponseEntity<>(
          collectionDto.getAuthorities().stream()
              .map(AuthorityDto::getId)
              .map(UUID::toString)
              .collect(Collectors.joining("\n")),
          headers,
          HttpStatus.OK
      );
    }

    return ResponseEntity.ok(collectionDto);
  }
}
