package org.damap.base.rest.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceConfigDO {

  private boolean publicAvailable;
  private boolean consentFormEnabled;
}
