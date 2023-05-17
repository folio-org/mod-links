package org.folio.entlinks.integration.marc;

import static java.util.Objects.nonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.JsonParser;

public class LinkedMarcJsonReader implements MarcReader {
  public static final String FIELD_REGEX = "[A-Z0-9][A-Z0-9][A-Z0-9]";
  public static final String SUBFIELD_REGEX = "[a-z0-9]";
  public static final int NO_ARRAY = 0;
  public static final int FIELDS_ARRAY = 1;
  public static final int SUBFIELDS_ARRAY = 2;
  public static final int CONTROLFIELD_ARRAY = 3;
  public static final int DATAFIELD_ARRAY = 4;
  public static final int SUBFIELD_ARRAY = 5;
  private final LinkedMarcFactory factory;
  private final JsonParser parser;
  private int parserLevel = 0;

  /**
   * Creates a MarcJsonReader from a supplied {@link InputStream}.
   *
   * @param is - an InputStream to read
   */
  public LinkedMarcJsonReader(final InputStream is) {
    parser = new JsonParser(
      JsonParser.OPT_INTERN_KEYWORDS | JsonParser.OPT_UNQUOTED_KEYWORDS | JsonParser.OPT_SINGLE_QUOTE_STRINGS);
    parser.setInput("MarcInput", new InputStreamReader(is), false);
    factory = new LinkedMarcFactory();
  }

  /**
   * Creates a MarcJsonReader from the supplied {@link Reader}.
   *
   * @param in - A Reader to use for input
   */
  public LinkedMarcJsonReader(final Reader in) {
    parser = new JsonParser(0);
    parser.setInput("MarcInput", in, false);
    factory = new LinkedMarcFactory();
  }

  /**
   * Returns <code>true</code> if there is a next record; else,
   * <code>false</code>.
   */
  @Override
  public boolean hasNext() {
    int code = parser.getEventCode();

    if (code == 0 || code == JsonParser.EVT_OBJECT_ENDED) {
      code = parser.next();
    }

    if (code == JsonParser.EVT_OBJECT_BEGIN) {
      return true;
    }

    if (code == JsonParser.EVT_INPUT_ENDED) {
      return false;
    }

    throw new MarcException("Malformed JSON input");
  }

  /**
   * Returns the next {@link Record}.
   */
  @Override
  public Record next() {
    int code = parser.getEventCode();
    Record record = null;
    ControlField cf = null;
    LinkedDataField df = null;
    Subfield sf = null;
    int inArray = NO_ARRAY;

    while (true) {
      final String mname = parser.getMemberName();

      switch (code) {
        case JsonParser.EVT_OBJECT_BEGIN -> {
          if (parserLevel == 0) {
            record = factory.newRecord();
          } else if (inArray == FIELDS_ARRAY && mname.matches(FIELD_REGEX)) {
            df = factory.newDataField();
            df.setTag(mname);
          }
          parserLevel++;
        }
        case JsonParser.EVT_OBJECT_ENDED -> {
          parserLevel--;
          if (parserLevel == 0) {
            return record;
          } else if (inArray == FIELDS_ARRAY && mname.matches(FIELD_REGEX) && nonNull(record)) {
            record.addVariableField(df);
            df = null;
          } else if (inArray == DATAFIELD_ARRAY && mname.matches("datafield") && nonNull(record)) {
            record.addVariableField(df);
            df = null;
          }
        }
        case JsonParser.EVT_ARRAY_BEGIN -> {
          switch (mname) {
            case "fields" -> inArray = FIELDS_ARRAY;
            case "subfields" -> inArray = SUBFIELDS_ARRAY;
            case "controlfield" -> inArray = CONTROLFIELD_ARRAY;
            case "datafield" -> inArray = DATAFIELD_ARRAY;
            case "subfield" -> inArray = SUBFIELD_ARRAY;
            default -> throw new IllegalStateException("Unexpected value: " + mname);
          }
        }
        case JsonParser.EVT_ARRAY_ENDED -> inArray = switch (mname) {
          case "fields", "datafield", "controlfield" -> NO_ARRAY;
          case "subfields" -> FIELDS_ARRAY;
          case "subfield" -> DATAFIELD_ARRAY;
          default -> throw new IllegalStateException("Unexpected value: " + mname);
        };
        case JsonParser.EVT_OBJECT_MEMBER -> {
          String value = parser.getMemberValue();
          if (JsonParser.isQuoted(value)) {
            value = JsonParser.stripQuotes(value);
          }
          value = value.replaceAll("⁄", "/");
          if (mname.equals("ind1") && nonNull(df)) {
            df.setIndicator1(getInd(value));
          } else if (mname.equals("ind2") && nonNull(df)) {
            df.setIndicator2(getInd(value));
          } else if (mname.equals("leader") && nonNull(record)) {
            record.setLeader(factory.newLeader(value));
          } else if (mname.equals("authorityId") && nonNull(df)) {
            df.setAuthorityId(UUID.fromString(value));
          } else if (mname.equals("authorityNaturalId") && nonNull(df)) {
            df.setNaturalId(value);
          } else if (mname.equals("linkingRuleId") && nonNull(df)) {
            df.setRuleId(Integer.parseInt(value));
          } else if (mname.equals("linkStatus") && nonNull(df)) {
            df.setLinkStatus(value);
          } else if (inArray == FIELDS_ARRAY && mname.matches(FIELD_REGEX) && nonNull(record)) {
            cf = factory.newControlField(mname, value);
            record.addVariableField(cf);
          } else if (inArray == SUBFIELDS_ARRAY && mname.matches(SUBFIELD_REGEX) && nonNull(df)) {
            sf = factory.newSubfield(mname.charAt(0), value);
            df.addSubfield(sf);
          } else if (inArray == CONTROLFIELD_ARRAY && mname.equals("tag")) {
            cf = factory.newControlField();
            cf.setTag(value);
          } else if (inArray == CONTROLFIELD_ARRAY && mname.equals("data") && nonNull(cf) && nonNull(record)) {
            cf.setData(value);
            record.addVariableField(cf);
          } else if (inArray == DATAFIELD_ARRAY && mname.equals("tag")) {
            df = factory.newDataField();
            df.setTag(value);
          } else if (inArray == DATAFIELD_ARRAY && mname.equals("ind") && nonNull(df)) {
            df.setIndicator1(getInd(value));
            df.setIndicator2(value.length() > 1 ? value.charAt(1) : ' ');
          } else if (inArray == SUBFIELD_ARRAY && mname.equals("code")) {
            sf = factory.newSubfield();
            sf.setCode(value.charAt(0));
          } else if (inArray == SUBFIELD_ARRAY && mname.equals("data") && nonNull(sf) && nonNull(df)) {
            sf.setData(value);
            df.addSubfield(sf);
          }
        }
        case JsonParser.EVT_INPUT_ENDED -> throw new MarcException("Premature end of input in JSON file");
      }
      code = parser.next();
    }
  }

  private char getInd(String value) {
    return value.length() >= 1 ? value.charAt(0) : ' ';
  }
}
