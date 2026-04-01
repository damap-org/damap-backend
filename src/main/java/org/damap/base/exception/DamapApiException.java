package org.damap.base.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class DamapApiException extends RuntimeException {
  private final ErrorDto payload;
  private final Response.Status status;

  public DamapApiException(ErrorDto payload, Response.Status status) {
    super(payload.details());
    this.payload = payload;
    this.status = status;
  }

  public DamapApiException(ErrorDto payload, Response.Status status, Throwable cause) {
    super(payload.details(), cause);
    this.payload = payload;
    this.status = status;
  }
}
