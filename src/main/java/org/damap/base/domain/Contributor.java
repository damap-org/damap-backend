package org.damap.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.Set;
import lombok.*;
import org.damap.base.annotations.gdpr.*;
import org.damap.base.enums.EContributorRole;
import org.hibernate.envers.Audited;

/** Contributor class. */
@Gdpr
@Data
@EqualsAndHashCode(callSuper = true, exclude = "dmp")
@ToString(exclude = "dmp")
@Entity
@Audited
public class Contributor extends PanacheEntity {

  @Version
  @Setter(AccessLevel.NONE)
  private long version;

  @GdprContext(properties = {"id", "project.title"})
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dmp_id", nullable = false, updatable = false)
  private Dmp dmp;

  @GdprBase
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "person_id")
  private Identifier personIdentifier;

  @GdprKey
  @Column(name = "university_id")
  private String universityId;

  @GdprBase private String mbox;

  @GdprBase
  @Column(name = "first_name")
  private String firstName;

  @GdprBase
  @Column(name = "last_name")
  private String lastName;

  @GdprExtended private String affiliation;

  @GdprExtended
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "affiliation_id")
  private Identifier affiliationId;

  private Boolean contact;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "contributor_roles")
  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  private Set<EContributorRole> contributorRoles;
}
