package org.folio.entlinks.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.folio.entlinks.LinkingPairType;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "linking_rules")
public class LinkingRules {

  @Id
  @Enumerated(EnumType.STRING)
  @Column(name = "linking_pair_type", unique = true)
  private LinkingPairType linkingPairType;

  @Type(type = "jsonb")
  @Column(name = "data", columnDefinition = "jsonb", nullable = false)
  private String data;

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) { return false; }
    LinkingRules instanceLink = (LinkingRules) o;
    return Objects.equals(linkingPairType, instanceLink.linkingPairType)
        && Objects.equals(data, instanceLink.data);
  }
}
