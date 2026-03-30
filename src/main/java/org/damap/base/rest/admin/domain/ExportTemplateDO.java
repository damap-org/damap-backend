package org.damap.base.rest.admin.domain;

import lombok.Data;

@Data
public class ExportTemplateDO {
  private Long id;
  private String name;
  private String templateCategory;
  private boolean active;
  private boolean isCustom;
}
