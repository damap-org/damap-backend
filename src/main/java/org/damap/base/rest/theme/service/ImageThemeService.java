package org.damap.base.rest.theme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Image;
import org.damap.base.rest.file_analysis.service.FileAnalysisService;
import org.hibernate.Hibernate;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
@JBossLog
public class ImageThemeService {

  @Inject FileAnalysisService fileAnalysisService;

  public enum AllowedImageType {
    PNG("image/png"),
    JPEG("image/jpeg"),
    JPG("image/jpg"),
    GIF("image/gif"),
    SVG("image/svg+xml"),
    WEBP("image/webp");

    private final String mimeType;

    AllowedImageType(String mimeType) {
      this.mimeType = mimeType;
    }

    public String getMimeType() {
      return mimeType;
    }

    public static boolean isAllowed(String mimeType) {
      if (mimeType == null) {
        return false;
      }
      for (AllowedImageType type : values()) {
        if (type.mimeType.equals(mimeType)) {
          return true;
        }
      }
      return false;
    }
  }

  // Maximum file size: 5MB
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  @Transactional
  public Image uploadImage(String imageKey, FileUpload file) {
    if (imageKey == null || imageKey.isBlank()) {
      throw new BadRequestException("Image key is required");
    }

    if (file == null || file.uploadedFile() == null) {
      throw new BadRequestException("File is required");
    }

    try {
      byte[] imageData = Files.readAllBytes(file.uploadedFile());

      if (imageData.length == 0) {
        throw new BadRequestException("No image data provided");
      }

      if (imageData.length > MAX_FILE_SIZE) {
        throw new ClientErrorException(
            "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB",
            Response.Status.REQUEST_ENTITY_TOO_LARGE);
      }

      String mimeType = fileAnalysisService.detectMimeType(file.uploadedFile().toFile());

      if (!isAllowedImageType(mimeType)) {
        throw new ClientErrorException(
            "Unsupported image type: " + mimeType + ". Allowed types: " + getAllowedTypesString(),
            Response.Status.UNSUPPORTED_MEDIA_TYPE);
      }

      Image existingImage = Image.find("imageKey", imageKey).firstResult();

      Image image;
      if (existingImage != null) {
        existingImage.setData(imageData);
        existingImage.setFilesize((long) imageData.length);
        existingImage.setMimeType(mimeType);
        existingImage.persist();
        log.info("Updated existing image for key: " + imageKey);
        image = existingImage;
      } else {
        image = new Image(imageKey, imageData, (long) imageData.length, mimeType);
        image.persist();
        log.info("Created new image for key: " + imageKey);
      }

      return image;

    } catch (IOException e) {
      throw new InternalServerErrorException("Failed to upload image", e);
    }
  }

  @Transactional
  public List<Image> getImages() {
    List<Image> images = Image.findAll().list();
    images.forEach(img -> Hibernate.initialize(img.getData()));
    return images;
  }

  @Transactional
  public void deleteImage(String imageKey) {
    if (imageKey == null || imageKey.isBlank()) {
      throw new BadRequestException("Image key is required");
    }

    if (Image.delete("imageKey", imageKey) == 0) {
      throw new NotFoundException("Image not found for key: " + imageKey);
    }
  }

  private String getAllowedTypesString() {
    StringBuilder sb = new StringBuilder();
    for (AllowedImageType type : AllowedImageType.values()) {
      sb.append(type.getMimeType()).append(", ");
    }
    return sb.toString().substring(0, sb.length() - 2);
  }

  private boolean isAllowedImageType(String mimeType) {
    return AllowedImageType.isAllowed(mimeType);
  }
}
