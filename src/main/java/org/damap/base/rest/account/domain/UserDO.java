package org.damap.base.rest.account.domain;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDO {

  @Size(max = 255)
  String userId;

  @Size(max = 255)
  private String displayName;

  @Size(max = 255)
  private String username;

  List<String> roles;

  List<String> apiKeys;
}
