package org.folio.entlinks.exception.type;

import org.folio.entlinks.exception.ResourceNotFoundException;

public class MarcAuthorityNotFoundException extends ResourceNotFoundException {

  private static final String RESOURCE_NAME = "Marc authority";

  public MarcAuthorityNotFoundException(Object id) {
    super(RESOURCE_NAME, id);
  }
}
