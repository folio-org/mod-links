package org.folio.entlinks.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeadingRef implements Serializable {

  private String headingType;

  private String heading;

  private Set<RelationshipType> relationshipType;

  public HeadingRef(HeadingRef other) {
    this.heading = other.heading;
    this.headingType = other.headingType;
    this.relationshipType = other.relationshipType;
  }

  public HeadingRef(String headingType, String heading) {
    this.headingType = headingType;
    this.heading = heading;
  }
}
