package org.damap.base.conversion;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EFunderIds;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/** TemplateSelectorServiceImpl class. */
@ApplicationScoped
@DefaultBean
@JBossLog
public class TemplateSelectorServiceImpl implements TemplateSelectorService {
  /** {@inheritDoc} */
  @Override
  public String selectTemplate(DmpDO dmpDO) {
    if (dmpDO.getProject() != null && dmpDO.getProject().getFunding() != null) {
      IdentifierDO funderIdentifier = dmpDO.getProject().getFunding().getFunderId();
      if (funderIdentifier != null
          && EIdentifierType.getFunderIdentifierTypeList().contains(funderIdentifier.getType())) {
        if (isHorizonEuropeTemplate(funderIdentifier)) {
          return "HORIZON_EUROPE";
        }
        if (isFWFTemplate(funderIdentifier)) {
          return "FWF";
        }
      }
    }
    // default export science europe template
    return "SCIENCE_EUROPE";
  }

  /** {@inheritDoc} */
  @Override
  public boolean isHorizonEuropeTemplate(IdentifierDO identifierDO) {
    return (EFunderIds.getEUFunderIds().contains(identifierDO.getIdentifier()));
  }

  /** {@inheritDoc} */
  @Override
  public boolean isFWFTemplate(IdentifierDO identifierDO) {
    return (EFunderIds.getFWFFunderIds().contains(identifierDO.getIdentifier()));
  }
}
