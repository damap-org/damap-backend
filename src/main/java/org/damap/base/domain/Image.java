package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"data"})
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "image")
public class Image extends PanacheEntity {

  @Column(name = "image_key", nullable = false, unique = true)
  private String imageKey;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "data", nullable = false)
  private byte[] data;

  @Column(name = "filesize", nullable = false)
  private Long filesize; // in bytes

  @Column(name = "mime_type", nullable = false)
  private String mimeType;
}
