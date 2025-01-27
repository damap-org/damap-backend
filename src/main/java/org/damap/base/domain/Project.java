package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.hibernate.Length;
import org.hibernate.envers.Audited;

/** Project class. */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Audited
public class Project extends PanacheEntity {

  @Version
  @Setter(AccessLevel.NONE)
  private long version;

  @Column(name = "university_id")
  private String universityId;

  private String title;

  @Column(length = Length.LONG32)
  private String description;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "funding_id")
  private Funding funding;

  @Column(name = "project_start")
  private Date start;

  @Column(name = "project_end")
  private Date end;

  @Column(name = "acronym")
  private String acronym;

  /**
   * Return the final value in the chain project.getFunding().getGrantIdentifier().getIdentifier()
   *
   * @return the value of the chain or null if any part of the chain is null.
   */
  public String getFundingGrantIdentifierIdentifier() {
    if (this.getFunding() == null
        || this.getFunding().getGrantIdentifier() == null
        || this.getFunding().getGrantIdentifier().getIdentifier() == null) {
      return null;
    }
    return this.getFunding().getGrantIdentifier().getIdentifier();
  }

  /**
   * Return the final value in the chain project.getFunding().getFunderIdentifier().getIdentifier().
   *
   * @return the value of the chain project.getFunding().getFunderIdentifier().getIdentifier() or
   *     null if any part of the chain is null.
   */
  public String getFundingFunderIdentifierIdentifier() {
    if (this.getFunding() == null
        || this.getFunding().getFunderIdentifier() == null
        || this.getFunding().getFunderIdentifier().getIdentifier() == null) {
      return null;
    }
    return this.getFunding().getFunderIdentifier().getIdentifier();
  }
}
