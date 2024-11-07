package org.folio.entlinks.integration.dto;

/**
 * Represents a parsed subfield within a MARC field.
 * Each subfield has a code and a value associated with it.
 *
 * @param code The code of the subfield.
 * @param value The value of the subfield.
 */
public record ParsedSubfield(char code, String value) { }
