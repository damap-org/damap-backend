package org.damap.base.conversion;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.*;
import org.damap.base.domain.*;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.mapper.ContributorDOMapper;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;

/**
 * This class describes the variable replacements flow for templates that keep to the science europe
 * guidelines
 */
@JBossLog
public abstract class AbstractTemplateExportScienceEuropeComponents
    extends AbstractTemplateExportSetup {

  protected Map<Long, String> datasetTableIDs = new HashMap<>();
  private List<String> projectCoordinatorOrInvestigatorIds = new ArrayList<>();

  /** {@inheritDoc} */
  @Override
  protected void exportSetup(long dmpId) {
    super.exportSetup(dmpId);
    determineDatasetIDs();
  }

  /** loadScienceEuropeContent. */
  public void loadScienceEuropeContent() {
    titlePage();
    contributorInformation();
    datasetsInformation();
    storageIntroInformation();
    storageInformation();
    dataQuality();
    sensitiveDataInformation();
    legalEthicalInformation();
    repoinfoAndToolsInformation();
    costInformation();
  }

  /** determineDatasetIDs. */
  public void determineDatasetIDs() {
    int newIDprogression = 0;
    int reuseIDprogression = 0;

    for (Dataset dataset : datasets) {
      if (dataset.getSource().equals(EDataSource.NEW)) {
        datasetTableIDs.put(dataset.id, "P" + ++newIDprogression);
      }
      if (dataset.getSource().equals(EDataSource.REUSED)) {
        datasetTableIDs.put(dataset.id, "R" + ++reuseIDprogression);
      }
    }
  }

  /** titlePage. */
  public void titlePage() {
    Project project = dmp.getProject();
    if (project == null) {
      return;
    }

    // mapping general information
    Integer titleLength = (project.getTitle() == null) ? 0 : project.getTitle().length();

    // variable project name
    if (titleLength / 25 > 2) // Title too long, need to be resized
    addReplacement(replacements, "[projectname]", project.getTitle() + "#oversize");
    else addReplacement(replacements, "[projectname]", project.getTitle());

    addReplacement(replacements, "[projectnameText]", project.getTitle());
    addReplacement(footerMap, "[projectnameText]", project.getTitle());

    addReplacement(replacements, "[acronym]", project.getAcronym());
    addReplacement(footerMap, "[acronym]", project.getAcronym());

    // variable project start date and end date
    addReplacement(
        replacements,
        "[startdate]",
        project.getStart() == null ? "" : formatter.format(project.getStart()));
    addReplacement(
        replacements,
        "[enddate]",
        project.getEnd() == null ? "" : formatter.format(project.getEnd()));

    // add funding program to funding item variables
    ProjectDO projectCRIS = null;
    if (project.getUniversityId() != null)
      projectCRIS = projectService.read(project.getUniversityId());

    titlePageFunding(project, projectCRIS);

    // variable project ID
    addReplacement(replacements, "[projectid]", project.getUniversityId());
  }

  private void titlePageFunding(Project project, ProjectDO projectCRIS) {
    List<String> fundingItems = new ArrayList<>();
    if (projectCRIS != null
        && projectCRIS.getFunding() != null
        && projectCRIS.getFunding().getFundingProgram() != null) {
      fundingItems.add(projectCRIS.getFunding().getFundingProgram());
    }
    // add grant number to funding item variables
    if (project.getFunding() != null
        && project.getFunding().getGrantIdentifier().getIdentifier() != null) {
      fundingItems.add(project.getFunding().getGrantIdentifier().getIdentifier());
    }
    // variable project funding, combination from funding item variables
    if (!fundingItems.isEmpty()) {
      addReplacement(replacements, "[grantid]", String.join(", ", fundingItems));
    } else {
      addReplacement(replacements, "[grantid]", "");
    }
    addReplacement(footerMap, "[grantid]", replacements.get("[grantid]"));
  }

  private String getContributorPersonIdentifier(Contributor contributor) {
    String identifier = null;
    Identifier personIdentifier = contributor != null ? contributor.getPersonIdentifier() : null;

    if (personIdentifier != null) {
      String contactIdentifierId = personIdentifier.getIdentifier();
      if (personIdentifier.getIdentifierType().equals(EIdentifierType.ORCID)) {
        identifier = "ORCID iD: " + contactIdentifierId;
      }
    }

    return identifier;
  }

  private String getContributorAffiliationIdentifier(Contributor contributor) {
    String identifier = null;
    Identifier affiliationIdentifier = contributor.getAffiliationId();

    if (affiliationIdentifier != null) {
      String contactAffiliationIdentifierId = affiliationIdentifier.getIdentifier();
      if (affiliationIdentifier.getIdentifierType().equals(EIdentifierType.ROR)) {
        identifier = "ROR: " + contactAffiliationIdentifierId;
      }
    }

    return identifier;
  }

  private void contactPersonInformation() {
    // mapping contact information

    Contributor contact = dmp.getContact();
    List<String> contactItems = new ArrayList<>();
    String contactName = "";
    String contactMail = "";
    String contactId = "";
    String contactAffiliation = "";
    String contactAffiliationId = "";

    if (contact == null) {
      addReplacement(replacements, "[contact]", joinWithComma(contactItems));
      return;
    }

    if (contact.getFirstName() != null && contact.getLastName() != null) {
      contactName = contact.getFirstName() + " " + contact.getLastName();
      contactItems.add(contactName);
    }

    if (contact.getMbox() != null) {
      contactMail = contact.getMbox();
      contactItems.add(contactMail);
    }

    contactId = getContributorPersonIdentifier(contact);
    if (contactId != null) {
      contactItems.add(contactId);
    }

    if (contact.getAffiliation() != null) {
      contactAffiliation = contact.getAffiliation();
      contactItems.add(contactAffiliation);
    }

    contactAffiliationId = getContributorAffiliationIdentifier(contact);
    if (contactAffiliationId != null) {
      contactItems.add(contactAffiliationId);
    }

    addReplacement(replacements, "[contact]", joinWithComma(contactItems));
  }

  private void projectCoordinatorInformation() {

    if (projectCoordinators == null) {
      addReplacement(replacements, "[coordinator]", "");
      return;
    }

    StringBuilder coordinatorInfos = new StringBuilder();
    // mapping project coordinator information
    for (ContributorDO contributor : projectCoordinators) {

      List<String> coordinatorProperties = new ArrayList<>();
      String coordinatorIdentifierId = "";
      String coordinatorAffiliationIdentifierId = "";

      if (contributor == null) {
        addReplacement(replacements, "[coordinator]", joinWithComma(coordinatorProperties));
        return;
      }

      if (contributor.getFirstName() != null && contributor.getLastName() != null)
        coordinatorProperties.add(contributor.getFirstName() + " " + contributor.getLastName());

      if (contributor.getMbox() != null) coordinatorProperties.add(contributor.getMbox());

      if (contributor.getPersonId() != null) {
        coordinatorIdentifierId = contributor.getPersonId().getIdentifier();

        if (Objects.equals(contributor.getPersonId().getType(), EIdentifierType.ORCID)) {
          String coordinatorId = "ORCID iD: " + coordinatorIdentifierId;
          coordinatorProperties.add(coordinatorId);
        }
      }

      if (contributor.getAffiliation() != null)
        coordinatorProperties.add(contributor.getAffiliation());

      if (contributor.getAffiliationId() != null) {
        coordinatorAffiliationIdentifierId = contributor.getAffiliationId().getIdentifier();

        if (Objects.equals(contributor.getAffiliationId().getType(), EIdentifierType.ROR)) {
          String coordinatorAffiliationIdentifierType = "ROR: ";
          String coordinatorAffiliationId =
              coordinatorAffiliationIdentifierType + coordinatorAffiliationIdentifierId;
          coordinatorProperties.add(coordinatorAffiliationId);
        }
      }

      if (contributor.getRole() != null) {
        String coordinatorRole = contributor.getRole().toString();
        coordinatorProperties.add(coordinatorRole);
      }

      coordinatorInfos.append(joinWithComma(coordinatorProperties));
      coordinatorInfos.append(";");

      this.projectCoordinatorOrInvestigatorIds.add(contributor.getUniversityId());
    }

    addReplacement(replacements, "[coordinator]", coordinatorInfos.toString());
  }

  private void dmpContributorInformation() {
    // mapping contributor information
    List<String> contributorList = new ArrayList<>();

    String contributorPerson = "";

    List<Contributor> contributors =
        Optional.ofNullable(dmp.getContributorList()).orElse(List.of());

    for (Contributor contributor : contributors) {
      List<String> contributorProperties = new ArrayList<>();
      String contributorName = "";
      String contributorMail = "";
      String contributorId = "";
      String contributorRole = "";
      String contributorAffiliation = "";
      String contributorAffiliationId = "";

      if (contributor.getFirstName() != null && contributor.getLastName() != null) {
        contributorName = contributor.getFirstName() + " " + contributor.getLastName();
        contributorProperties.add(contributorName);
      }

      if (contributor.getMbox() != null) {
        contributorMail = contributor.getMbox();
        contributorProperties.add(contributorMail);
      }

      contributorId = getContributorPersonIdentifier(contributor);
      if (contributorId != null) {
        contributorProperties.add(contributorId);
      }

      if (contributor.getAffiliation() != null) {
        contributorAffiliation = contributor.getAffiliation();
        contributorProperties.add(contributorAffiliation);
      }

      contributorAffiliationId = getContributorAffiliationIdentifier(contributor);
      if (contributorAffiliationId != null) {
        contributorProperties.add(contributorAffiliationId);
      }

      if (contributor.getContributorRole() != null) {
        contributorRole = contributor.getContributorRole().getRole();
        contributorProperties.add(contributorRole);
      }

      if ((contributor.getContributorRole() != null
              && (contributor.getContributorRole().equals(EContributorRole.PRINCIPAL_INVESTIGATOR)
                  || contributor.getContributorRole().equals(EContributorRole.PROJECT_LEADER)
                  || contributor.getContributorRole().equals(EContributorRole.PROJECT_COORDINATOR)))
          || this.projectCoordinatorOrInvestigatorIds.contains(contributor.getUniversityId())) {
        continue;
      }

      contributorPerson = joinWithComma(contributorProperties);
      contributorList.add(contributorPerson);
    }

    addReplacement(replacements, "[contributors]", String.join(";", contributorList));
  }

  /** contributorInformation. */
  public void contributorInformation() {
    contactPersonInformation();
    projectCoordinatorInformation();
    dmpContributorInformation();
  }

  /** costInformation. */
  public void costInformation() {
    StringBuilder newDescription = new StringBuilder();
    if (Boolean.TRUE.equals(dmp.getCostsExist())) {
      addReplacement(
          replacements,
          "[costs]",
          loadResourceService.loadVariableFromResource(prop, "costs.avail"));
    } else {
      addReplacement(
          replacements, "[costs]", loadResourceService.loadVariableFromResource(prop, "costs.no"));
      addReplacement(replacements, "[costsDescriptions]", "");
    }
    if (!costList.isEmpty()) {
      boolean longDescriptionExists = false;
      for (Cost cost : costList) {
        if (cost.getDescription() != null && cost.getDescription().length() >= 255) {
          newDescription.append("Description for \"");
          newDescription.append(cost.getTitle()).append("\": ");
          newDescription.append(cost.getDescription()).append(";");
          longDescriptionExists = true;
        }
        addReplacement(replacements, "[costsDescriptions]", newDescription.toString());
      }
      addReplacement(
          replacements,
          "[costCoverage]",
          (longDescriptionExists ? ";" : "")
              + loadResourceService.loadVariableFromResource(prop, "costCoverage"));
    } else {
      addReplacement(replacements, "[costsDescriptions]", "");
      addReplacement(replacements, "[costCoverage]", "");
    }
  }

  /** datasetsInformation. */
  public void datasetsInformation() {
    addReplacement(replacements, "[datageneration]", dmp.getDataGeneration());
    addReplacement(replacements, "[documentation]", dmp.getDocumentation());
    if (dmp.getTargetAudience() != null)
      addReplacement(replacements, "[targetaudience]", dmp.getTargetAudience());
    else addReplacement(replacements, "[targetaudience]", "");

    StringBuilder reusedDescription = new StringBuilder();
    List<Dataset> reusedDatasets = getReusedDatasets();
    for (int i = 0; i < reusedDatasets.size(); i++) {
      Dataset dataset = reusedDatasets.get(i);
      if (dataset.getDescription() != null && !dataset.getDescription().isEmpty()) {
        reusedDescription.append("Description for \"");
        reusedDescription.append(dataset.getTitle()).append("\": ;");
        reusedDescription.append(dataset.getDescription()).append(";");
      }
    }

    addReplacement(replacements, "[reuseddatadescription]", reusedDescription.toString());

    StringBuilder newDescription = new StringBuilder();
    List<Dataset> newDatasets = getNewDatasets();
    for (int i = 0; i < newDatasets.size(); i++) {
      Dataset dataset = newDatasets.get(i);
      if (dataset.getDescription() != null && !dataset.getDescription().isEmpty()) {
        newDescription.append("Description for \"");
        newDescription.append(dataset.getTitle()).append("\": ;");
        newDescription.append(dataset.getDescription()).append(";");
      }
    }

    addReplacement(replacements, "[produceddatadescription]", newDescription.toString());
  }

  /** storageIntroInformation */
  public void storageIntroInformation() {
    String coordinatorFullName = null;

    List<Contributor> contributorPool = dmp.getContributorList();
    for (ContributorDO contributorDO : projectCoordinators) {
      contributorPool.add(ContributorDOMapper.mapDOtoEntity(contributorDO, new Contributor()));
    }

    // Data Manager
    for (Contributor contributor : contributorPool) {
      if (contributor != null
          && contributor.getContributorRole() != null
          && contributor.getContributorRole().equals(EContributorRole.DATA_MANAGER)) {
        coordinatorFullName = contributor.getFirstName() + " " + contributor.getLastName();
        break;
      }
    }

    if (coordinatorFullName == null) {
      // Project Leader
      for (Contributor contributor : contributorPool) {
        if (contributor != null
            && contributor.getContributorRole() != null
            && contributor.getContributorRole().equals(EContributorRole.PROJECT_LEADER)) {
          coordinatorFullName = contributor.getFirstName() + " " + contributor.getLastName();
          break;
        }
      }
    }

    if (coordinatorFullName == null) {
      // Principal Investigator
      for (Contributor contributor : contributorPool) {
        if (contributor != null
            && contributor.getContributorRole() != null
            && contributor.getContributorRole().equals(EContributorRole.PRINCIPAL_INVESTIGATOR)) {
          coordinatorFullName = contributor.getFirstName() + " " + contributor.getLastName();
          break;
        }
      }
    }

    if (coordinatorFullName == null) {
      // Project Coordinator
      for (Contributor contributor : contributorPool) {
        if (contributor != null
            && contributor.getContributorRole() != null
            && contributor.getContributorRole().equals(EContributorRole.PROJECT_COORDINATOR)) {
          coordinatorFullName = contributor.getFirstName() + " " + contributor.getLastName();
          break;
        }
      }
    }

    if (coordinatorFullName == null) {
      // Contact person as fallback option 1
      if (dmp.getContact() != null) {
        coordinatorFullName =
            dmp.getContact().getFirstName() + " " + dmp.getContact().getLastName();
      }
    }

    if (coordinatorFullName == null) {
      // Generic string as fallback option 2
      coordinatorFullName = "the project leader ";
    }

    boolean usesExternalStorage =
        dmp.getHostList().stream().anyMatch(ExternalStorage.class::isInstance);
    boolean usesInternalStorage = dmp.getHostList().stream().anyMatch(Storage.class::isInstance);

    String propName = "storageIntro.none";
    if (usesExternalStorage && !usesInternalStorage) {
      propName = "storageIntro.external";
    } else if (usesInternalStorage && !usesExternalStorage) {
      propName = "storageIntro.internal";
    } else if (usesInternalStorage && usesExternalStorage) {
      propName = "storageIntro.both";
    }

    String storageIntroReplacement = loadResourceService.loadVariableFromResource(prop, propName);
    storageIntroReplacement = storageIntroReplacement.replace("[name]", coordinatorFullName);

    addReplacement(replacements, "[storageintro]", storageIntroReplacement);
  }

  /** storageInformation. */
  public void storageInformation() {
    List<Host> hostList = dmp.getHostList();
    String storageVar = "";

    for (Host host : hostList) {
      List<Distribution> distributions = host.getDistributionList();
      String hostVar = "";
      StringBuilder distVar = new StringBuilder();

      if (host.getTitle() != null) {
        hostVar = host.getTitle();
      }

      for (Distribution dist : distributions) {
        distVar
            .append(datasetTableIDs.get(dist.getDataset().getId()))
            .append(" (")
            .append(dist.getDataset().getTitle())
            .append(")");
        if (distributions.indexOf(dist) + 1 < distributions.size()) distVar.append(", ");
      }

      if (Storage.class.isAssignableFrom(
          host.getClass())) { // only write information related to the storage, repository will be
        // written in section 5
        if (!distVar.toString().isEmpty()) {
          String storageDescription = "";
          InternalStorageTranslation storageTranslation =
              internalStorageTranslationRepo.getInternalStorageById(
                  ((Storage) host).getInternalStorageId().id, "eng");

          if (storageTranslation == null) {
            storageTranslation =
                internalStorageTranslationRepo
                    .getAllInternalStorageTranslationsByStorageId(
                        ((Storage) host).getInternalStorageId().id)
                    .get(0);
          }

          storageDescription = storageTranslation.getDescription();
          storageVar =
              storageVar.concat(
                  distVar
                      + " "
                      + loadResourceService.loadVariableFromResource(prop, "distributionStorage")
                      + " "
                      + hostVar);
          if (storageDescription != null && !storageDescription.isEmpty()) {
            storageVar = storageVar.concat(": " + storageDescription);
          }
        }
      } else if (ExternalStorage.class.isAssignableFrom(
          host.getClass())) { // case for external storage, will have null host Id
        if (!distVar.toString().isEmpty()) {
          storageVar =
              storageVar.concat(
                  distVar
                      + " "
                      + loadResourceService.loadVariableFromResource(prop, "distributionStorage")
                      + " "
                      + hostVar
                      + ".");
          if (dmp.getExternalStorageInfo() != null && !dmp.getExternalStorageInfo().isEmpty()) {
            storageVar =
                storageVar.concat(
                    " "
                        + loadResourceService.loadVariableFromResource(prop, "distributionExternal")
                        + " "
                        + dmp.getExternalStorageInfo().toLowerCase());
          }
        }
      }

      if (hostList.indexOf(host) + 1 < hostList.size() && !distributions.isEmpty()) {
        storageVar = storageVar.concat(";");
      }
    }

    addReplacement(replacements, "[storage]", storageVar);
  }

  /** dataQuality. */
  public void dataQuality() {
    String metadata = "";

    if (dmp.getMetadata() == null) {
      addReplacement(
          replacements,
          "[metadata]",
          loadResourceService.loadVariableFromResource(prop, "metadata.no"));
    } else {
      if (dmp.getMetadata().isEmpty()) {
        addReplacement(
            replacements,
            "[metadata]",
            loadResourceService.loadVariableFromResource(prop, "metadata.no"));
      } else {
        metadata = dmp.getMetadata();
        if (metadata.charAt(metadata.length() - 1) != '.') {
          metadata += '.';
        }
        addReplacement(
            replacements,
            "[metadata]",
            metadata + " " + loadResourceService.loadVariableFromResource(prop, "metadata.avail"));
      }
    }

    if (dmp.getStructure() == null) {
      addReplacement(
          replacements,
          "[dataorganisation]",
          loadResourceService.loadVariableFromResource(prop, "dataOrganisation.no"));
    } else {
      if (dmp.getStructure().isEmpty()) {
        addReplacement(
            replacements,
            "[dataorganisation]",
            loadResourceService.loadVariableFromResource(prop, "dataOrganisation.no"));
      } else {
        addReplacement(replacements, "[dataorganisation]", dmp.getStructure());
      }
    }

    if (dmp.getDataQuality() != null && !dmp.getDataQuality().isEmpty()) {
      StringBuilder dataQuality = new StringBuilder();
      dataQuality.append(
          loadResourceService.loadVariableFromResource(prop, "dataQualityControl.avail"));
      List<String> dataQualityList = new ArrayList<>();
      for (int i = 0; i < dmp.getDataQuality().size(); i++) {
        if (dmp.getDataQuality().get(i).equals(EDataQualityType.OTHERS)
            && dmp.getOtherDataQuality() != null
            && !dmp.getOtherDataQuality().isEmpty()) dataQualityList.add(dmp.getOtherDataQuality());
        else dataQualityList.add(dmp.getDataQuality().get(i).toString());
      }
      dataQuality.append(" ").append(joinWithCommaAnd(dataQualityList)).append(".");
      addReplacement(replacements, "[dataqualitycontrol]", dataQuality.toString());
    } else {
      addReplacement(
          replacements,
          "[dataqualitycontrol]",
          loadResourceService.loadVariableFromResource(prop, "dataQualityControl.default"));
    }
  }

  /** sensitiveDataInformation. */
  public void sensitiveDataInformation() {
    log.debug("sensitive data part");

    String sensitiveData = "";
    if (Boolean.TRUE.equals(dmp.getSensitiveData())) {
      String sensitiveDataSentence =
          loadResourceService.loadVariableFromResource(prop, "sensitive.avail");
      String sensitiveDataset = "";
      String datasetSentence = "";
      String sensitiveDataMeasure = "";
      String authorisedAccess = "";
      List<String> sensitiveDatasetList = new ArrayList<>();

      for (Dataset dataset : datasets) {
        if (dataset.getSensitiveData() != null && dataset.getSensitiveData()) {
          sensitiveDataset = datasetTableIDs.get(dataset.getId()) + " (" + dataset.getTitle() + ")";
          sensitiveDatasetList.add(sensitiveDataset);
        }
      }

      if (!sensitiveDatasetList.isEmpty()) {
        datasetSentence =
            " " + loadResourceService.loadVariableFromResource(prop, "sensitive.avail.data") + " ";
        sensitiveDataset = joinWithCommaAnd(sensitiveDatasetList) + ". ";
      } else {
        datasetSentence = ". ";
      }

      List<String> dataSecurityList = new ArrayList<>();

      if (dmp.getSensitiveDataSecurity() != null) {
        for (ESecurityMeasure securityMeasure : dmp.getSensitiveDataSecurity()) {
          if (securityMeasure.equals(ESecurityMeasure.OTHER)
              && dmp.getOtherDataSecurityMeasures() != null
              && !dmp.getOtherDataSecurityMeasures().isEmpty())
            dataSecurityList.add(dmp.getOtherDataSecurityMeasures());
          else dataSecurityList.add(securityMeasure.toString());
        }
      }

      if (dataSecurityList.isEmpty()) {
        sensitiveDataMeasure =
            loadResourceService.loadVariableFromResource(prop, "sensitiveMeasure.no");
      } else {
        // security measurement size defined is/or usage
        if (dataSecurityList.size() == 1) {
          sensitiveDataMeasure =
              loadResourceService.loadVariableFromResource(prop, "sensitiveMeasure.avail")
                  + " "
                  + joinWithCommaAnd(dataSecurityList)
                  + " "
                  + loadResourceService.loadVariableFromResource(prop, "sensitiveMeasure.singular");
        } else {
          sensitiveDataMeasure =
              loadResourceService.loadVariableFromResource(prop, "sensitiveMeasure.avail")
                  + " "
                  + joinWithCommaAnd(dataSecurityList)
                  + " "
                  + loadResourceService.loadVariableFromResource(prop, "sensitiveMeasure.multiple");
        }
      }

      if (dmp.getSensitiveDataAccess() != null && (!dmp.getSensitiveDataAccess().isEmpty())) {
        authorisedAccess =
            " "
                + loadResourceService.loadVariableFromResource(prop, "sensitiveAccess")
                + " "
                + dmp.getSensitiveDataAccess()
                + " "
                + loadResourceService.loadVariableFromResource(prop, "sensitiveAccess.avail");
      }

      sensitiveData =
          sensitiveDataSentence
              + datasetSentence
              + sensitiveDataset
              + sensitiveDataMeasure
              + authorisedAccess;

    } else {
      sensitiveData = loadResourceService.loadVariableFromResource(prop, "sensitive.no");
    }
    addReplacement(replacements, "[sensitivedata]", sensitiveData);
  }

  /** repoInformation. */
  protected void repoInformation() {
    String repoInformation = "";
    Set<Repository> repositories = new HashSet<>();
    List<String> repoTexts = new ArrayList<>();

    for (Dataset dataset : datasets) {
      repositories.addAll(dataset.getRepositories());
    }

    if (!repositories.isEmpty()) {

      repositories.forEach(
          repo ->
              repoTexts.add(
                  repositoriesService.getDescription(repo.getRepositoryId())
                      + " "
                      + repositoriesService.getRepositoryURL(repo.getRepositoryId())));

      repoInformation = String.join("; ", repoTexts);
    }

    addReplacement(
        replacements,
        "[repoinformation]",
        (!repoInformation.isEmpty() ? "Repository description: ;" : "")
            + repoInformation
            + (!repoInformation.isEmpty() ? "" : ";"));
  }

  protected void closeAndRestrictedDataInformation() {
    String closedRestrictedReasons = "";
    if (dmp.getClosedAccessInfo() != null && !dmp.getClosedAccessInfo().isBlank()) {
      closedRestrictedReasons =
          loadResourceService.loadVariableFromResource(prop, "closeddatasetreasons.intro")
              + ";"
              + dmp.getClosedAccessInfo();
    }

    if (dmp.getRestrictedAccessInfo() != null && !dmp.getRestrictedAccessInfo().isBlank()) {
      if (!closedRestrictedReasons.isEmpty()) {
        closedRestrictedReasons += ";";
      }

      closedRestrictedReasons +=
          loadResourceService.loadVariableFromResource(prop, "restricteddatasetreasons.intro")
              + ";"
              + dmp.getRestrictedAccessInfo();

      closedRestrictedReasons += ";";
    }

    if (!closedRestrictedReasons.isEmpty()) {
      closedRestrictedReasons += ";";
    }

    addReplacement(replacements, "[closedrestricteddatasetreasons]", closedRestrictedReasons);
  }

  /** repoinfoAndToolsInformation. */
  public void repoinfoAndToolsInformation() {
    repoInformation();
    closeAndRestrictedDataInformation();

    if (dmp.getTools() != null) {
      if (!Objects.equals(dmp.getTools(), "")) {
        addReplacement(
            replacements,
            "[tools]",
            loadResourceService.loadVariableFromResource(prop, "tools.avail")
                + " "
                + dmp.getTools());
      } else {
        addReplacement(
            replacements,
            "[tools]",
            loadResourceService.loadVariableFromResource(prop, "tools.no"));
      }
    } else {
      addReplacement(
          replacements, "[tools]", loadResourceService.loadVariableFromResource(prop, "tools.no"));
    }

    if (dmp.getRestrictedDataAccess() != null) {
      if (!Objects.equals(dmp.getRestrictedDataAccess(), "")) {
        addReplacement(
            replacements,
            "[restrictedAccessInfo]",
            loadResourceService.loadVariableFromResource(prop, "restrictedAccess.avail")
                + " "
                + dmp.getRestrictedDataAccess());
      } else {
        addReplacement(
            replacements,
            "[restrictedAccessInfo]",
            loadResourceService.loadVariableFromResource(prop, ""));
      }
    } else {
      addReplacement(
          replacements,
          "[restrictedAccessInfo]",
          loadResourceService.loadVariableFromResource(prop, ""));
    }
  }

  /** legalEthicalInformation. */
  public void legalEthicalInformation() {
    personalDataText();
    intellectualPropertyText();
    ethicalIssuesText();
  }

  /** personalDataText. */
  public void personalDataText() {
    log.debug("personal data part");
    String personalData = "";
    if (Boolean.TRUE.equals(dmp.getPersonalData())) {
      personalData = loadResourceService.loadVariableFromResource(prop, "personal.avail") + " ";

      List<String> personalDatasetList = new ArrayList<>();
      for (Dataset dataset : datasets) {
        if (Boolean.TRUE.equals(dataset.getPersonalData())) {
          personalDatasetList.add(
              datasetTableIDs.get(dataset.getId()) + " (" + dataset.getTitle() + ")");
        }
      }
      // add dataset list if available
      if (!personalDatasetList.isEmpty()) {
        personalData += joinWithCommaAnd(personalDatasetList);
        personalData +=
            " " + loadResourceService.loadVariableFromResource(prop, "personalDataset") + " ";
      }

      if (!dmp.getPersonalDataCompliance().isEmpty()) {
        List<String> dataComplianceList = new ArrayList<>();
        for (EComplianceType personalCompliance : dmp.getPersonalDataCompliance()) {
          if (personalCompliance.equals(EComplianceType.OTHER)
              && dmp.getOtherPersonalDataCompliance() != null
              && !dmp.getOtherPersonalDataCompliance().isEmpty())
            dataComplianceList.add(dmp.getOtherPersonalDataCompliance());
          else dataComplianceList.add(personalCompliance.toString());
        }
        personalData +=
            loadResourceService.loadVariableFromResource(prop, "personalCompliance") + " ";
        personalData += joinWithCommaAnd(dataComplianceList) + ".";
      }
    } else {
      personalData = loadResourceService.loadVariableFromResource(prop, "personal.no");
    }
    addReplacement(replacements, "[personaldata]", personalData);
  }

  // Section 4b: legal restriction
  /** intellectualPropertyText. */
  public void intellectualPropertyText() {
    log.debug("legal restriction part");

    String legalRestrictionText = "";

    if (Boolean.TRUE.equals(dmp.getLegalRestrictions())) {

      // determine document list
      if (!dmp.getLegalRestrictionsDocuments().isEmpty()) {
        legalRestrictionText =
            loadResourceService.loadVariableFromResource(prop, "legal.avail") + " ";
        List<String> agreementList = new ArrayList<>();
        for (EAgreement agreement : dmp.getLegalRestrictionsDocuments()) {
          if (agreement.equals(EAgreement.OTHER)
              && dmp.getOtherLegalRestrictionsDocument() != null
              && !dmp.getOtherLegalRestrictionsDocument().isEmpty())
            agreementList.add(dmp.getOtherLegalRestrictionsDocument());
          else agreementList.add(agreement.toString());
        }
        legalRestrictionText += joinWithCommaAnd(agreementList) + ". ";
      } else {
        legalRestrictionText =
            loadResourceService.loadVariableFromResource(prop, "legal.avail.default") + " ";
      }

      // determine dataset list
      List<String> datasetList = new ArrayList<>();
      for (Dataset dataset : datasets) {
        if (Boolean.TRUE.equals(dataset.getLegalRestrictions())) {
          datasetList.add(datasetTableIDs.get(dataset.getId()) + " (" + dataset.getTitle() + ")");
        }
      }
      if (!datasetList.isEmpty()) {
        legalRestrictionText +=
            loadResourceService.loadVariableFromResource(prop, "legalDataset") + " ";
        legalRestrictionText += joinWithCommaAnd(datasetList) + ". ";
      }

      // add legal restrictions comment if available
      if (dmp.getLegalRestrictionsComment() != null
          && !dmp.getLegalRestrictionsComment().isEmpty()) {
        legalRestrictionText +=
            loadResourceService.loadVariableFromResource(prop, "legalComment")
                + " "
                + dmp.getLegalRestrictionsComment();
      }

      if (dmp.getDataRightsAndAccessControl() != null
          && !dmp.getDataRightsAndAccessControl().isEmpty()) {
        legalRestrictionText +=
            ";"
                + loadResourceService.loadVariableFromResource(prop, "legalRights.owners")
                + ": "
                + dmp.getDataRightsAndAccessControl();
      } else {
        legalRestrictionText +=
            ";" + loadResourceService.loadVariableFromResource(prop, "legalRights.owners.no");
      }
    } else {
      if (dmp.getDataRightsAndAccessControl() != null) {
        legalRestrictionText =
            loadResourceService.loadVariableFromResource(prop, "legalRights.owners")
                + ": "
                + dmp.getDataRightsAndAccessControl();
      } else {
        legalRestrictionText =
            loadResourceService.loadVariableFromResource(prop, "legalRights.owners.no");
      }
    }
    addReplacement(replacements, "[legalrestriction]", legalRestrictionText);
  }

  // Section 4c: ethical issues
  /** ethicalIssuesText. */
  public void ethicalIssuesText() {
    log.debug("ethical part");
    String ethicalStatement = "";

    if (Boolean.TRUE.equals(dmp.getHumanParticipants())
        || Boolean.TRUE.equals(dmp.getEthicalIssuesExist())) {
      ethicalStatement =
          loadResourceService.loadVariableFromResource(prop, "ethicalStatement") + " ";
    } else {
      ethicalStatement = loadResourceService.loadVariableFromResource(prop, "ethical.no") + " ";
    }

    if (Boolean.TRUE.equals(dmp.getCommitteeReviewed())) {
      ethicalStatement +=
          loadResourceService.loadVariableFromResource(prop, "ethicalReviewed.avail");
    }

    addReplacement(replacements, "[ethicalissues]", ethicalStatement);
  }

  // Number conversion for data size in section 1
  private static final char[] SUFFIXES = {'K', 'M', 'G', 'T', 'P', 'E'};

  /**
   * format.
   *
   * @param number a long
   * @return a {@link java.lang.String} object
   */
  protected static String format(long number) {
    if (number < 1000) {
      // No need to format this
      return String.valueOf(number);
    }
    // Convert to a string
    final String string = String.valueOf(number);
    // The suffix we're using, 1-based
    final int magnitude = (string.length() - 1) / 3;
    // The number of digits we must show before the prefix
    final int digits = (string.length() - 1) % 3 + 1;

    // Build the string
    char[] value = new char[digits + 4];

    for (int i = 0; i < digits; i++) {
      value[i] = string.charAt(i);
    }
    int valueLength = digits;
    // Can and should we add a decimal point and an additional number?
    if (digits == 1 && string.charAt(1) != '0') {
      value[valueLength++] = '.';
      value[valueLength++] = string.charAt(1);
    }
    value[valueLength++] = ' ';
    value[valueLength++] = SUFFIXES[magnitude - 1];
    return new String(value, 0, valueLength);
  }

  // All tables variables replacement
  // Takes care of filling tables and deletes certain empty tables
  /**
   * tableContent.
   *
   * @param document a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param xwpfTables a {@link java.util.List} object
   */
  public void tableContent(XWPFDocument document, List<XWPFTable> xwpfTables) {
    for (XWPFTable xwpfTable : new ArrayList<>(xwpfTables)) {
      XWPFTableRow tableIdentifierRow = xwpfTable.getRow(1);
      if (tableIdentifierRow != null) {

        // Making sure that static tables without identifiers are handled correctly.
        XWPFTableCell tableIdentifierCell = tableIdentifierRow.getCell(1);
        String tableIdentifier = "";
        if (tableIdentifierCell != null) {
          if (tableIdentifierCell.getParagraphs().get(0).getRuns().isEmpty()) {
            tableIdentifierCell.getParagraphs().get(0).createRun();
          }
          tableIdentifier = tableIdentifierCell.getParagraphs().get(0).getRuns().get(0).getText(0);
        }

        tableIdentifier = tableIdentifier == null ? "" : tableIdentifier;

        switch (tableIdentifier) {
          case ("[datasetTable]"):
            composeTableNewDatasets(xwpfTable);
            break;
          case ("[reusedDatasetTable]"):
            composeTableReusedDatasets(document, xwpfTable);
            break;
          case ("[datasetAccessTable]"):
            composeTableDataAccess(xwpfTable);
            break;
          case ("[datasetPublicationTable]"):
            composeTableDatasetPublication(xwpfTable);
            break;
          case ("[datasetRepositoryTable]"):
            composeTableDatasetRepository(xwpfTable);
            break;
          case ("[datasetDeleteTable]"):
            composeTableDatasetDeletion(document, xwpfTable);
            break;
          case ("[costTable]"):
            composeTableCost(document, xwpfTable);
            break;
          default:
            break;
        }
      }
    }

    // prevents replacing table variables of deleted tables
    for (XWPFTable table : getAllTables(document)) {
      replaceTableVariables(table, replacements);
    }
  }

  /**
   * composeTableNewDatasets.
   *
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableNewDatasets(XWPFTable xwpfTable) {
    log.debug("Export steps: New Dataset Table");

    List<Dataset> newDatasets = getNewDatasets();
    if (!newDatasets.isEmpty()) {
      for (int i = 0; i < newDatasets.size(); i++) {

        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);
        Dataset dataset = newDatasets.get(i);

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception e) {
        }

        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(datasetTableIDs.get(dataset.id));
        docVar.add(Optional.ofNullable(dataset.getTitle()).orElse(""));

        if (dataset.getType() != null) {
          docVar.add(
              dataset.getType().stream()
                  .map(EDataType::getValue)
                  .collect(Collectors.joining(", ")));
        } else {
          docVar.add("");
        }

        docVar.add(Optional.ofNullable(dataset.getFileFormat()).orElse(""));

        if (dataset.getSize() != null) {
          docVar.add(DatasetSizeRange.getLabelForSize(dataset.getSize()));
        } else {
          docVar.add("");
        }

        if (dataset.getSensitiveData() != null) {
          if (Boolean.TRUE.equals(dataset.getSensitiveData())) {
            docVar.add("yes");
          } else {
            docVar.add("no");
          }
        } else {
          docVar.add("no");
        }

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
    } else {
      // clean row
      ArrayList<String> emptyContent = new ArrayList<>(Arrays.asList("", "", "", "", "", "", ""));
      insertTableCells(
          xwpfTable, xwpfTable.getRows().get(xwpfTable.getRows().size() - 1), emptyContent);
    }
    // end of dynamic table rows code
    xwpfTable.removeRow(1);
  }

  /**
   * getNewDatasets.
   *
   * @return a {@link java.util.List} object
   */
  public List<Dataset> getNewDatasets() {
    return datasets.stream()
        .filter(dataset -> dataset.getSource().equals(EDataSource.NEW))
        .toList();
  }

  /**
   * getReusedDatasets.
   *
   * @return a {@link java.util.List} object
   */
  public List<Dataset> getReusedDatasets() {
    return datasets.stream()
        .filter(dataset -> dataset.getSource().equals(EDataSource.REUSED))
        .toList();
  }

  /**
   * composeTableReusedDatasets.
   *
   * @param document a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableReusedDatasets(XWPFDocument document, XWPFTable xwpfTable) {
    log.debug("Export steps: Reused Dataset Table");

    List<Dataset> reusedDatasets = getReusedDatasets();
    if (!reusedDatasets.isEmpty()) {
      for (int i = 0; i < reusedDatasets.size(); i++) {

        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);
        Dataset dataset = reusedDatasets.get(i);

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception e) {
        }

        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(datasetTableIDs.get(dataset.id));
        docVar.add(Optional.ofNullable(dataset.getTitle()).orElse(""));

        if (dataset.getDatasetIdentifier() != null) {
          docVar.add(dataset.getDatasetIdentifier().getIdentifier());
        } else {
          docVar.add("");
        }

        if (dataset.getLicense() != null) {
          // TODO second String license option for reused datasets.
          // TODO use addHyperlinkRun to create hyperlinks - see publication table
          docVar.add("");
        } else {
          docVar.add("");
        }

        if (dataset.getSensitiveData() != null) {
          if (Boolean.TRUE.equals(dataset.getSensitiveData())) {
            docVar.add("yes");
          } else {
            docVar.add("no");
          }
        } else {
          docVar.add("no");
        }

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
      xwpfTable.removeRow(1);
    } else {
      removeTableAndParagraphAbove(document, xwpfTable);
    }
  }

  /**
   * composeTableDataAccess.
   *
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableDataAccess(XWPFTable xwpfTable) {
    log.debug("Export steps: Data Access Table");

    List<Dataset> newDatasets = getNewDatasets();
    List<Dataset> reusedDatasets = getReusedDatasets();
    if (!datasets.isEmpty()) {
      // this split is so that produced and reused datasets are not mixed in the table, to improve
      // readability
      insertComposeTableDataAccess(xwpfTable, reusedDatasets);
      insertComposeTableDataAccess(xwpfTable, newDatasets);
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
    } else {
      // clean row
      ArrayList<String> emptyContent = new ArrayList<String>(Arrays.asList("", "", "", ""));
      insertTableCells(
          xwpfTable, xwpfTable.getRows().get(xwpfTable.getRows().size() - 1), emptyContent);
    }
    xwpfTable.removeRow(1);
    replaceTableVariables(xwpfTable, replacements);
  }

  private void insertComposeTableDataAccess(XWPFTable xwpfTable, List<Dataset> currentDatasets) {
    for (int i = 0; i < currentDatasets.size(); i++) {

      XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
      XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);

      try {
        newRow = insertNewTableRow(sourceTableRow, i + 2);
      } catch (Exception e) {
      }

      ArrayList<String> docVar = new ArrayList<String>();
      docVar.add(datasetTableIDs.get(currentDatasets.get(i).id));

      if (currentDatasets.get(i).getSelectedProjectMembersAccess() != null) {
        docVar.add(
            currentDatasets.get(i).getSelectedProjectMembersAccess().toString().toLowerCase());
      } else {
        docVar.add("");
      }

      if (currentDatasets.get(i).getOtherProjectMembersAccess() != null) {
        docVar.add(currentDatasets.get(i).getOtherProjectMembersAccess().toString().toLowerCase());
      } else {
        docVar.add("");
      }

      if (currentDatasets.get(i).getPublicAccess() != null) {
        docVar.add(currentDatasets.get(i).getPublicAccess().toString().toLowerCase());
      } else {
        docVar.add("");
      }

      insertTableCells(xwpfTable, newRow, docVar);
    }
  }

  /**
   * composeTableDatasetPublication.
   *
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableDatasetPublication(XWPFTable xwpfTable) {
    log.debug("Export steps: Data Publication Table");

    List<Dataset> newDatasets = getNewDatasets();
    if (!newDatasets.isEmpty()) {
      for (int i = 0; i < newDatasets.size(); i++) {

        XWPFTableRow sourceTableRow = xwpfTable.getRow(i + 2);
        XWPFTableRow newRow = null;

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception ignored) {
        }

        ArrayList<String> docVar = new ArrayList<String>();
        docVar.add(datasetTableIDs.get(newDatasets.get(i).id));

        if (newDatasets.get(i).getDataAccess() != null) {
          docVar.add(newDatasets.get(i).getDataAccess().toString());
        } else {
          docVar.add("");
        }

        if (newDatasets.get(i).getDataAccess() == EDataAccessType.CLOSED) {
          docVar.add("");
        } else if (newDatasets.get(i).getStart() != null) {
          docVar.add(formatter.format(newDatasets.get(i).getStart()));
        } else {
          // if null set default value of project end date minus two months
          if (dmp.getProject() != null && dmp.getProject().getEnd() != null) {
            docVar.add(
                formatter.format(
                    Date.from(
                        ZonedDateTime.from(
                                dmp.getProject()
                                    .getEnd()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault()))
                            .minusMonths(2)
                            .toInstant())));
          } else docVar.add("");
        }
        List<Repository> repositories = newDatasets.get(i).getRepositories();
        List<String> repositoryTitles = repositories.stream().map(Repository::getTitle).toList();
        docVar.add(joinWithComma(repositoryTitles));

        Set<EIdentifierType> pids = new HashSet<>();
        for (Repository repository : repositories) {
          pids.addAll(repositoriesService.getPidSystems(repository.getRepositoryId()));
        }
        docVar.add(joinWithComma(pids.stream().map(EIdentifierType::getType).toList()));

        // suppress license information for closed datasets
        if (newDatasets.get(i).getLicense() != null
            && !newDatasets.get(i).getDataAccess().equals(EDataAccessType.CLOSED))
          docVar.add(newDatasets.get(i).getLicense().getAcronym());
        else docVar.add("");

        insertTableCells(xwpfTable, newRow, docVar);

        if (newDatasets.get(i).getLicense() != null
            && !newDatasets.get(i).getDataAccess().equals(EDataAccessType.CLOSED)) {

          ELicense license = newDatasets.get(i).getLicense();
          XWPFParagraph paragraph = newRow.getCell(5).getParagraphs().get(0);
          turnRunIntoHyperlinkRun(paragraph.getRuns().get(0), license.getUrl());
          commitTableRows(xwpfTable);
        }
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
    } else {
      // clean row
      ArrayList<String> emptyContent =
          new ArrayList<String>(Arrays.asList("", "", "", "", "", "", ""));
      insertTableCells(
          xwpfTable, xwpfTable.getRows().get(xwpfTable.getRows().size() - 1), emptyContent);
    }
    xwpfTable.removeRow(1);
  }

  /**
   * composeTableDatasetRepository.
   *
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableDatasetRepository(XWPFTable xwpfTable) {
    log.debug("Export steps: Dataset Repository Table");

    List<Dataset> newDatasets =
        getNewDatasets().stream().filter(dataset -> !dataset.getDelete()).toList();
    if (!newDatasets.isEmpty()) {
      for (int i = 0; i < newDatasets.size(); i++) {

        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception e) {
        }

        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(datasetTableIDs.get(newDatasets.get(i).id));
        List<Repository> repositories = newDatasets.get(i).getRepositories();
        List<String> repositoryTitles = repositories.stream().map(Repository::getTitle).toList();
        docVar.add(joinWithComma(repositoryTitles));

        if (newDatasets.get(i).getRetentionPeriod() != null)
          docVar.add(newDatasets.get(i).getRetentionPeriod() + " years");
        else docVar.add("");

        if (newDatasets.get(i).getDmp().getTargetAudience() != null)
          docVar.add(newDatasets.get(i).getDmp().getTargetAudience());
        else docVar.add("");

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
    } else {
      // clean row
      ArrayList<String> emptyContent = new ArrayList<String>(Arrays.asList("", "", "", ""));
      insertTableCells(
          xwpfTable, xwpfTable.getRows().get(xwpfTable.getRows().size() - 1), emptyContent);
    }
    xwpfTable.removeRow(1);

    // this snippet serves to merge the last column, which contains the [targetaudience] text valid
    // for all rows.
    CTVMerge vMerge = CTVMerge.Factory.newInstance();
    List<XWPFTableRow> rowList = xwpfTable.getRows();
    for (int i = 1; i < rowList.size(); i++) {
      xwpfTable.getRow(i).getCell(3).getCTTc().getTcPr().setVMerge(vMerge);
    }
    commitTableRows(xwpfTable);
  }

  /**
   * composeTableDatasetDeletion.
   *
   * @param document a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void composeTableDatasetDeletion(XWPFDocument document, XWPFTable xwpfTable) {
    log.debug("Export steps: Dataset Deletion Table");

    if (!deletedDatasets.isEmpty()) {

      for (int i = 0; i < deletedDatasets.size(); i++) {
        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception e) {
        }

        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(datasetTableIDs.get(deletedDatasets.get(i).id));

        if (deletedDatasets.get(i).getTitle() != null)
          docVar.add(deletedDatasets.get(i).getTitle());
        else docVar.add("");

        if (deletedDatasets.get(i).getDateOfDeletion() != null)
          docVar.add(formatter.format(deletedDatasets.get(i).getDateOfDeletion()));
        else docVar.add("");

        if (deletedDatasets.get(i).getReasonForDeletion() != null)
          docVar.add(deletedDatasets.get(i).getReasonForDeletion());
        else docVar.add("");

        if (deletedDatasets.get(i).getDeletionPerson() != null)
          docVar.add(
              deletedDatasets.get(i).getDeletionPerson().getFirstName()
                  + " "
                  + deletedDatasets.get(i).getDeletionPerson().getLastName());
        else docVar.add("");

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
      xwpfTable.removeRow(1);
    } else {
      removeTableAndParagraphAbove(document, xwpfTable);
    }
  }

  /**
   * composeTableCost.
   *
   * @param xwpfTable a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   * @param xwpfDocument a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public void composeTableCost(XWPFDocument xwpfDocument, XWPFTable xwpfTable) {
    log.debug("Export steps: Cost Table");
    Float totalCost = 0f;
    if (!costList.isEmpty()) {
      for (int i = 0; i < costList.size(); i++) {
        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);
        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception ignored) {
        }
        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(Optional.ofNullable(costList.get(i).getTitle()).orElse(""));
        docVar.add((costList.get(i).getType() != null) ? costList.get(i).getType().toString() : "");
        if (costList.get(i).getDescription() != null) {
          docVar.add(
              (costList.get(i).getDescription().length() < 255)
                  ? costList.get(i).getDescription()
                  : "");
        } else {
          docVar.add("");
        }
        docVar.add(Optional.ofNullable(costList.get(i).getCurrencyCode()).orElse("€"));
        if (costList.get(i).getValue() != null) {
          docVar.add(
              NumberFormat.getNumberInstance(Locale.GERMAN).format(costList.get(i).getValue()));
          totalCost += costList.get(i).getValue();
        } else docVar.add("");

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 2);
      xwpfTable.removeRow(1);
    } else {
      removeTable(xwpfDocument, xwpfTable);
    }
    Optional<Cost> costCurrencyTotal =
        costList.stream().filter(cost -> cost.getCurrencyCode() != null).findFirst();
    addReplacement(
        replacements,
        "[costcurrency]",
        costCurrencyTotal.isPresent() ? costCurrencyTotal.get().getCurrencyCode() : "");
    addReplacement(
        replacements,
        "[costtotal]",
        NumberFormat.getNumberInstance(Locale.GERMAN).format(totalCost));
  }
}
