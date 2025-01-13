package org.damap.base.domain.listener;

import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.DamapRevisionEntity;
import org.damap.base.security.SecurityService;
import org.hibernate.envers.RevisionListener;

/** DamapRevisionListener class. */
@JBossLog
public class DamapRevisionListener implements RevisionListener {

  /** {@inheritDoc} */
  @Override
  public void newRevision(Object revisionEntity) {
    if (revisionEntity instanceof DamapRevisionEntity) {

      final String userId = CDI.current().select(SecurityService.class).get().getUserId();
      final String username = CDI.current().select(SecurityService.class).get().getDisplayName();
      ((DamapRevisionEntity) revisionEntity).setChangedById(userId);
      ((DamapRevisionEntity) revisionEntity).setChangedBy(username);
    }
  }
}
