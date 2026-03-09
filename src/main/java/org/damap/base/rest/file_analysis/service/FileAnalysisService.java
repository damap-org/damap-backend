package org.damap.base.rest.file_analysis.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.tika.Tika;
import org.damap.base.domain.Dataset;
import org.damap.base.enums.EAccessRight;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataSource;
import org.damap.base.enums.ELicense;
import org.damap.base.rest.dmp.domain.MultipartBodyDO;
import org.damap.base.rest.file_analysis.mapper.FileAnalysisMapper;

@ApplicationScoped
@JBossLog
public class FileAnalysisService {

  // Tika instance is thread-safe and can be reused
  private final Tika tika = new Tika();

  public String detectMimeType(File file) {
    if (file == null || !file.exists()) {
      return "";
    }
    try {
      // Tika reads the magic bytes of the file securely
      return tika.detect(file);
    } catch (IOException e) {
      log.error("Failed to detect mime type", e);
      return "";
    }
  }

  public Dataset analyseFile(MultipartBodyDO data) {
    Dataset dataset = new Dataset();

    dataset.setSource(EDataSource.NEW);
    dataset.setDataAccess(EDataAccessType.OPEN);
    dataset.setLicense(ELicense.CCBY);
    dataset.setSelectedProjectMembersAccess(EAccessRight.WRITE);
    dataset.setOtherProjectMembersAccess(EAccessRight.WRITE);
    dataset.setPublicAccess(EAccessRight.NONE);
    dataset.setRetentionPeriod(10);

    File file = data.file;
    if (file != null && file.exists()) {
      String mimeType = detectMimeType(file);
      FileAnalysisMapper.updateDatasetWithFileMetadata(dataset, file.length(), mimeType);
    }
    return dataset;
  }
}
