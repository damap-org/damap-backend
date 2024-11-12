package org.damap.base.rest.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BannerDO {

  @Size(max = 255)
  private String title;

  @Size(max = 255)
  private String description;

  private Boolean dismissible;

  @Size(max = 255)
  private String color;
}
