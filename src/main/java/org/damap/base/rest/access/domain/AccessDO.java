package org.damap.base.rest.access.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Date;
import lombok.Data;
import org.damap.base.enums.EFunctionRole;

/** AccessDO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessDO {

  private Long id;

  @NotNull @Positive private long dmpId;

  @NotNull private EFunctionRole role;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date start;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date until;

  private String identifier;

  private String firstName;

  private String lastName;

  private String mbox;
}
