package org.damap.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.*;
import org.damap.base.enums.*;
import org.hibernate.Length;
import org.hibernate.envers.Audited;

/** Dataset class. */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "dmp")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited()
public class Dataset extends PanacheEntity {

  @Version
  @Setter(AccessLevel.NONE)
  private long version;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dmp_id")
  @EqualsAndHashCode.Exclude
  private Dmp dmp;

  private String title;

  @ElementCollection(targetClass = EDataType.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "data_type")
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private List<EDataType> type;

  @Column(name = "file_format")
  private String fileFormat;

  @Column(name = "data_size")
  private Long size;

  @Column(name = "description", length = Length.LONG32)
  private String description;

  @Column(name = "personal_data")
  private Boolean personalData;

  @Column(name = "sensitive_data")
  private Boolean sensitiveData;

  @Column(name = "legal_restrictions")
  private Boolean legalRestrictions;

  @Enumerated(EnumType.STRING)
  @Column(name = "license")
  private ELicense license;

  @Column(name = "start_date")
  private Date start;

  @Column(name = "reference_hash")
  private String referenceHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "data_access")
  private EDataAccessType dataAccess;

  @OneToMany(
      mappedBy = "dataset",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  private List<Distribution> distributionList = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "sel_project_members_access")
  private EAccessRight selectedProjectMembersAccess;

  @Enumerated(EnumType.STRING)
  @Column(name = "other_project_members_access")
  private EAccessRight otherProjectMembersAccess;

  @Enumerated(EnumType.STRING)
  @Column(name = "public_access")
  private EAccessRight publicAccess;

  @Column(name = "delete_data")
  private Boolean delete;

  @Column(name = "date_of_deletion")
  private Date dateOfDeletion;

  @Column(name = "reason_for_deletion")
  private String reasonForDeletion;

  @ManyToOne()
  @JoinColumn(name = "deletion_person_id")
  private Contributor deletionPerson;

  @Column(name = "retention_period")
  private Integer retentionPeriod;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "dataset_pid")
  private Identifier datasetIdentifier;

  @Enumerated(EnumType.STRING)
  @Column(name = "dataset_source")
  private EDataSource source;

  /**
   * getRepositories.
   *
   * @return a {@link java.util.List} object
   */
  public List<Repository> getRepositories() {
    List<Repository> repositories = new ArrayList<>();
    for (Distribution distribution : this.getDistributionList()) {
      if (Repository.class.isAssignableFrom(distribution.getHost().getClass()))
        repositories.add((Repository) distribution.getHost());
    }
    return repositories;
  }

  /**
   * getId.
   *
   * @return a {@link java.lang.Long} object
   */
  @EqualsAndHashCode.Include
  public Long getId() {
    return id;
  }

  /**
   * Return the value of the chain dataset.getDmp().getTargetAudience()
   *
   * @return the value of the targetAudience or null if any part of the chain is null.
   */
  public String getDmpTargetAudience() {
    if (this.getDmp() == null) {
      return null;
    }
    return this.getDmp().getTargetAudience();
  }
}
