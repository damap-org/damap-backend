package org.damap.base.rest.config.domain;

import java.net.URL;
import java.util.List;
import org.damap.base.enums.EContributorRole;

/** Represents all config options that need to be uniquely configured per tenant */
public interface DamapTenantAwareConfig {
  String title();

  Fields fields();

  String projectService();

  List<ServiceConfig> personServices();

  String elsevierPureDescriptionClassification();

  List<PureRoleClassification> elsevierPureContributorRoleClassifications();

  String elsevierPureProjectLeadRoleClassification();

  String elsevierPureBackend();

  String elsevierPureEndpointUrl();

  String elsevierPureApiKey();

  URL elsevierPureProjectsFile();

  URL elsevierPurePersonsFile();

  interface Fields {
    boolean ethicalReportEnabled();
  }

  interface ServiceConfig {
    String className();

    String displayText();

    String queryValue();
  }

  interface PureRoleClassification {
    String pureRoleUri();

    EContributorRole contributorRole();
  }
}
