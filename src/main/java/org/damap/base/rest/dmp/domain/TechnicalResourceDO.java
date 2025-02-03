package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalResourceDO {

  @Size(max = 255)
  @NotBlank(message = "technical resource name cannot be blank")
  @NotNull(message = "technical resource name cannot be null") private String name;

  @Size(max = 4000)
  private String description;
}
