package org.damap.base.rest.admin.mapper;

import lombok.experimental.UtilityClass;
import org.damap.base.domain.ExportTemplate;
import org.damap.base.rest.admin.domain.ExportTemplateDO;

@UtilityClass
public class ExportTemplateDOMapper {

  public static ExportTemplateDO mapEntityToDO(
      ExportTemplate entity, ExportTemplateDO exportTemplateDO) {
    exportTemplateDO.setId(entity.id);
    exportTemplateDO.setName(entity.getName());
    exportTemplateDO.setTemplateKey(entity.getTemplateKey());
    exportTemplateDO.setActive(entity.isActive());
    exportTemplateDO.setCustom(entity.isCustom());
    return exportTemplateDO;
  }
}
