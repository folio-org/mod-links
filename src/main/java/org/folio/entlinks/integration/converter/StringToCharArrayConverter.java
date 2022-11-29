package org.folio.entlinks.integration.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StringToCharArrayConverter implements AttributeConverter<char[], String> {

  @Override
  public String convertToDatabaseColumn(char[] attribute) {
    return String.valueOf(attribute);
  }

  @Override
  public char[] convertToEntityAttribute(String dbData) {
    return dbData.toCharArray();
  }
}
