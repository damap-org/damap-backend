package org.damap.base.rest.account.domain;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiKeyDO {

  @Size(max = 255)
  private String name;

  @Size(max = 255)
  private String value;
}
