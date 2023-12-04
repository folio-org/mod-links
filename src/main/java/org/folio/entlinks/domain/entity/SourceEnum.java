package org.folio.entlinks.domain.entity;

public enum SourceEnum {
  FOLIO("folio"),

  LOCAL("local");

  private final String value;

  SourceEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
