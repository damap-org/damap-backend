package org.damap.base.exception;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class GlobalServerExceptionMapper {

  Logger log = Logger.getLogger(GlobalServerExceptionMapper.class);

  @ServerExceptionMapper(DamapApiException.class)
  public RestResponse<ErrorDto> mapException(DamapApiException e) {
    log.error(e.getPayload().details(), e);
    return RestResponse.ResponseBuilder.create(e.getStatus(), e.getPayload())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .build();
  }
}
