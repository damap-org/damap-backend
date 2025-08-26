package org.damap.base.rest.theme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.ColorTheme;

@ApplicationScoped
@JBossLog
public class ColorThemeService {

  @Transactional
  public ColorTheme getTheme() {
    return ColorTheme.findAll().firstResult();
  }

  @Transactional
  public ColorTheme uploadTheme(@Valid ColorTheme colorTheme) {
    ColorTheme.deleteAll();
    colorTheme.persist();
    return colorTheme;
  }
}
