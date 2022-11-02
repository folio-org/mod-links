package org.folio.entlinks.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @Column(name = "record_type", nullable = false)
  private String recordType;

  @NotNull
  @Column(name = "rules", columnDefinition = "jsonb", nullable = false)
  private String rules;

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) { return false; }
    LinkingRules instanceLink = (LinkingRules) o;
    return id != null && Objects.equals(id, instanceLink.id);
  }
}
