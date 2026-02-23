package org.damap.base.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Dataset;
import org.damap.base.domain.ExportTemplate;
import org.damap.base.rest.dmp.domain.MultipartBodyDO;
import org.damap.base.rest.fits.service.FitsService;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
@JBossLog
public class ExportTemplateService {

  @Inject FitsService fitsService;

  @Transactional
  public ExportTemplate uploadTemplate(String name, String templateKey, FileUpload file) {
    try {
      MultipartBodyDO multipartBody = new MultipartBodyDO();
      multipartBody.file = file.uploadedFile().toFile();

      Dataset analyzedData = fitsService.analyseFile(multipartBody);

      String mimeType = analyzedData.getFileFormat();
      if (!"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              .equals(mimeType)
          && !"application/msword".equals(mimeType)) {

        log.error("FITS rejected upload. Detected MIME type: " + mimeType);
        throw new BadRequestException("Wrong format. Template must be a .docx file.");
      }
      ExportTemplate template = new ExportTemplate();
      template.setName(name);
      template.setTemplateKey(templateKey);
      template.setData(Files.readAllBytes(file.uploadedFile()));
      template.setActive(true);
      template.setCustom(true);
      template.persist();
      return template;
    } catch (IOException e) {
      log.error("Failed to read template file.", e);
      throw new RuntimeException("Failed to upload template.", e);
    }
  }

  @Transactional
  public void toggleStatus(Long id) {
    ExportTemplate t = ExportTemplate.findById(id);
    if (t != null) {
      if (t.isActive()) {
        long activeCount = ExportTemplate.count("active = true");
        if (activeCount <= 1) {
          throw new jakarta.ws.rs.BadRequestException("At least one template must remain active.");
        }
      }

      t.setActive(!t.isActive());
      t.persist();
    }
  }

  @Transactional
  public InputStream getTemplateStream(Long id) {
    ExportTemplate template = ExportTemplate.findById(id);
    if (template != null && template.getData() != null) {
      return new ByteArrayInputStream(template.getData());
    }
    return null;
  }

  @Transactional
  public void deleteTemplate(Long id) {
    ExportTemplate t = ExportTemplate.findById(id);
    // Only allow deleting custom templates, never the seeded defaults
    if (t != null && t.isCustom()) {
      if (t.isActive() && ExportTemplate.count("active = true") <= 1) {
        throw new BadRequestException("Cannot delete the last active template.");
      }
      t.delete();
    }
  }
}
