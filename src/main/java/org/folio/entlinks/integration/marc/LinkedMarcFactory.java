package org.folio.entlinks.integration.marc;

import org.marc4j.marc.impl.MarcFactoryImpl;

public class LinkedMarcFactory extends MarcFactoryImpl {

  @Override
  public LinkedDataField newDataField() {
    return new LinkedDataField();
  }
}
