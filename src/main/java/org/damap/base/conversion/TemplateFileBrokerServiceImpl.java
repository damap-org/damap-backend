package org.damap.base.conversion;

/*
   extend this class in your custom project, in order to load you custom resources/templates
*/

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.jbosslog.JBossLog;

/** TemplateFileBrokerServiceImpl class. */
@ApplicationScoped
@DefaultBean
@JBossLog
public class TemplateFileBrokerServiceImpl implements TemplateFileBrokerService {

  /** {@inheritDoc} */
  @Override
  public Properties getScienceEuropeTemplateResource() {
    return loadResource("org/damap/base/template/scienceEuropeTemplate.resource");
  }

  /** {@inheritDoc} */
  @Override
  public InputStream loadScienceEuropeTemplate() {
    return loadTemplate("org/damap/base/template/scienceEuropeTemplate.docx");
  }

  /** {@inheritDoc} */
  @Override
  public Properties getFWFTemplateResource() {
    return loadResource("org/damap/base/template/FWFTemplate.resource");
  }

  /** {@inheritDoc} */
  @Override
  public InputStream loadFWFTemplate() {
    return loadTemplate("org/damap/base/template/FWFTemplate.docx");
  }

  /** {@inheritDoc} */
  @Override
  public InputStream loadHorizonEuropeTemplate() {
    return loadTemplate("org/damap/base/template/horizonEuropeTemplate.docx");
  }

  /** {@inheritDoc} */
  @Override
  public Properties getHorizonEuropeTemplateResource() {
    return loadResource("org/damap/base/template/horizonEuropeTemplate.resource");
  }

  /**
   * loadResource.
   *
   * @param resource a {@link java.lang.String} object
   * @return a {@link java.util.Properties} object
   */
  public Properties loadResource(String resource) {
    Properties prop = new Properties();
    try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(resource)) {
      // load a properties file
      prop.load(input);
    } catch (IOException ex) {
      log.error("could not load resource file: " + resource);
    }
    return prop;
  }

  /**
   * loadTemplate.
   *
   * @param template a {@link java.lang.String} object
   * @return a {@link java.io.InputStream} object
   */
  public InputStream loadTemplate(String template) {
    ClassLoader classLoader = getClass().getClassLoader();
    return classLoader.getResourceAsStream(template);
  }
}
