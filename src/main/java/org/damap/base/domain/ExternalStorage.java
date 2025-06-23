package org.damap.base.domain;

import jakarta.persistence.*;
import jakarta.persistence.Access;
import lombok.*;
import org.hibernate.envers.Audited;

/** ExternalStorage class. */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("EXTERNAL_STORAGE")
@Access(AccessType.FIELD)
@Audited
@Table(name = "external_storage")
public class ExternalStorage extends Host {

  private String url;

  @Column(name = "backup_frequency")
  private String backupFrequency;

  @Column(name = "storage_location")
  private String storageLocation;

  @Column(name = "backup_location")
  private String backupLocation;

  // This field indicates whether this specific external storage is managed internally or not.
  // It is possible, that a storage is not indicated as internal storage (by the admin in the Admin
  // UI),
  // but is still managed internally. In this case, this field should be set to true.
  @Column(name = "is_managed_internally")
  private Boolean isManagedInternally;
}
