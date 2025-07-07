package org.damap.base.rest.projects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@inheritDoc}
 *
 * @deprecated Use {@link org.damap.base.rest.dmp.domain.ProjectSupplementDO} instead.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class ProjectSupplementDO extends org.damap.base.rest.dmp.domain.ProjectSupplementDO {}
