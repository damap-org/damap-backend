package org.damap.base.rest.admin.mapper;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.domain.ExportTemplate;
import org.damap.base.rest.admin.domain.ExportTemplateDO;

@UtilityClass
public class ExportTemplateDOMapper {

  public static ExportTemplateDO mapEntityToDO(
      ExportTemplate entity, ExportTemplateDO exportTemplateDO) {
    exportTemplateDO.setId(entity.id);
    exportTemplateDO.setName(entity.getName());
    exportTemplateDO.setTemplateCategory(entity.getTemplateCategory());
    exportTemplateDO.setActive(entity.isActive());
    exportTemplateDO.setCustom(entity.isCustom());
    return exportTemplateDO;
  }

  public static List<ExportTemplateDO> mapEntityListToDOList(List<ExportTemplate> templates) {
    if (templates == null) {
      return List.of();
    }
    return templates.stream().map(entity -> mapEntityToDO(entity, new ExportTemplateDO())).toList();
  }
}
