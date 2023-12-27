package org.folio.entlinks.domain.entity;

public enum AuthoritySourceFileSource {
  FOLIO("folio"),

  LOCAL("local"),

  CONSORTIUM("consortium");

  private final String value;

  AuthoritySourceFileSource(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
