package org.damap.base.rest.access.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDO {

  private String identifier;

  private String name;

  private String firstName;

  private String lastName;

  private String email;
}
