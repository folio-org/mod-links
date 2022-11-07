package org.folio.entlinks;

public enum LinkingRecords {
  INSTANCE_AUTHORITY("instance-authority");

  private final String value;

  LinkingRecords(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
