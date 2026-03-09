package org.damap.base.rest.file_analysis.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.domain.Dataset;
import org.damap.base.enums.*;

@UtilityClass
public class FileAnalysisMapper {

  public Dataset updateDatasetWithFileMetadata(Dataset dataset, long sizeInBytes, String mimeType) {
    dataset.setSize(mapSize(sizeInBytes));
    dataset.setFileFormat(mimeType != null ? mimeType : "");
    dataset.setType(mapFileFormat(mimeType));
    return dataset;
  }

  private List<EDataType> mapFileFormat(String mimetype) {
    List<EDataType> type = new ArrayList<>();

    if (mimetype == null || mimetype.isBlank()) {
      type.add(EDataType.OTHER);
      return type;
    }

    // Tika provides standard mime types, so we can check prefixes directly
    if (mimetype.startsWith("image/")) {
      type.add(EDataType.IMAGES);
      return type;
    }

    switch (mimetype) {
      case "application/msword":
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
      case "application/vnd.ms-excel":
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
      case "application/pdf":
        type.add(EDataType.STANDARD_OFFICE_DOCUMENTS);
        break;
      case "text/plain":
        type.add(EDataType.PLAIN_TEXT);
        break;
      case "application/gzip":
      case "application/java-archive":
      case "application/x-7z-compressed":
      case "application/zip":
      case "application/x-tar":
      case "application/vnd.rar":
      case "application/x-bzip":
      case "application/x-bzip2":
        type.add(EDataType.ARCHIVED_DATA);
        break;
      case "text/javascript":
      case "application/javascript":
        type.add(EDataType.SOURCE_CODE);
        break;
      default:
        // Fallback if not specifically caught
        type.add(EDataType.OTHER);
        break;
    }
    return type;
  }

  private Long mapSize(Long size) {
    if (size == null || size < 0L) return -1L;
    if (size < 100_000_000L) return 99_999_999L; // < 100 MB
    if (size < 1_000_000_000L) return 999_999_999L; // 100 - 1000 MB
    if (size < 5_000_000_000L) return 4_999_999_999L; // 1 - 5 GB
    if (size < 20_000_000_000L) return 19_999_999_999L; // 5 - 20 GB
    if (size < 50_000_000_000L) return 49_999_999_999L; // 20 - 50 GB
    if (size < 100_000_000_000L) return 99_999_999_999L; // 50 - 100 GB
    if (size < 500_000_000_000L) return 499_999_999_999L; // 100 - 500 GB
    if (size < 1_000_000_000_000L) return 999_999_999_999L; // 500 - 1000 GB
    if (size < 5_000_000_000_000L) return 4_999_999_999_999L; // 1 - 5 TB
    if (size < 10_000_000_000_000L) return 9_999_999_999_999L; // 5 - 10 TB
    if (size < 100_000_000_000_000L) return 99_999_999_999_999L; // 10 - 100 TB
    if (size < 500_000_000_000_000L) return 499_999_999_999_999L; // 100 - 500 TB
    if (size < 1_000_000_000_000_000L) return 999_999_999_999_999L; // 500 - 1000 TB

    return 1_500_000_000_000_000L; // > 1 PB
  }
}
