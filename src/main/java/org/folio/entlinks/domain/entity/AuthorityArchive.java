package org.folio.entlinks.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@ToString
@Table(name = "authority_archive")
public class AuthorityArchive extends AuthorityBase {

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Authority that = (Authority) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }
}
