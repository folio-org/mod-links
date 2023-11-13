package org.folio.entlinks.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.folio.entlinks.domain.entity.base.Identifiable;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@With
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "authority_archive")
public class AuthorityArchive extends MetadataEntity implements Persistable<UUID>, Identifiable<UUID> {

  @Id
  @Column(name = Authority.ID_COLUMN, nullable = false)
  private UUID id;

  @Column(name = Authority.NATURAL_ID_COLUMN)
  private String naturalId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = Authority.SOURCE_FILE_COLUMN, nullable = false)
  private AuthoritySourceFile authoritySourceFile;

  @Column(name = Authority.SOURCE_COLUMN)
  private String source;

  @Column(name = Authority.HEADING_COLUMN)
  private String heading;

  @Column(name = Authority.HEADING_TYPE_COLUMN)
  private String headingType;

  @Version
  @Column(name = Authority.VERSION_COLUMN, nullable = false)
  private int version;

  @Column(name = Authority.SUBJECT_HEADING_CODE_COLUMN)
  private Character subjectHeadingCode;

  @Column(name = Authority.SFT_HEADINGS_COLUMN)
  @JdbcTypeCode(SqlTypes.JSON)
  private List<HeadingRef> sftHeadings;

  @Column(name = Authority.SAFT_HEADINGS_COLUMN)
  @JdbcTypeCode(SqlTypes.JSON)
  private List<HeadingRef> saftHeadings;

  @Column(name = Authority.IDENTIFIERS_COLUMN)
  @JdbcTypeCode(SqlTypes.JSON)
  private List<AuthorityIdentifier> identifiers;

  @Column(name = Authority.NOTES_COLUMN)
  @JdbcTypeCode(SqlTypes.JSON)
  private List<AuthorityNote> notes;

  @Column(name = Authority.DELETED_COLUMN)
  private boolean deleted = false;

  @Transient
  private boolean isNew = true;

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    AuthorityArchive that = (AuthorityArchive) o;
    return id != null && Objects.equals(id, that.id);
  }

  @PostLoad
  @PrePersist
  void markNotNew() {
    this.isNew = false;
  }
}
