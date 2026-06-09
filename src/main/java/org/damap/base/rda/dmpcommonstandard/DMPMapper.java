package org.damap.base.rda.dmpcommonstandard;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.damap.base.enums.EDataKind;
import org.damap.base.r3data.RepositoriesService;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ExternalStorageDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.RepositoryDO;

/**
 * This class implements DMP conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public final class DMPMapper extends AbstractMapper {

  private final ProjectMapper projectMapper;
  private final ContributorMapper contributorMapper;
  private final CostsMapper costsMapper;
  private final DatasetMapper datasetMapper;

  /** Initialize the mapper with the default settings (strict mode turned on). */
  public DMPMapper() {
    this(true);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public DMPMapper(boolean strict) {
    this(
        strict,
        new ProjectMapper(strict),
        new ContributorMapper(strict),
        new CostsMapper(strict),
        new DatasetMapper(strict));
  }

  /**
   * Initialize the mapper with an option to turn off strict mode and the ability to override
   * submappers. Only use non-strict mode when a human has to review the DMP afterward, never use it
   * for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   * @param projectMapper contains the pre-configured {@link ProjectMapper}.
   * @param costsMapper contains a pre-configured {@link CostsMapper}.
   * @param datasetMapper contains a pre-configured {@link DatasetMapper}.
   */
  public DMPMapper(
      boolean strict,
      ProjectMapper projectMapper,
      ContributorMapper contributorMapper,
      CostsMapper costsMapper,
      DatasetMapper datasetMapper) {
    super(strict);
    this.projectMapper = projectMapper;
    this.contributorMapper = contributorMapper;
    this.costsMapper = costsMapper;
    this.datasetMapper = datasetMapper;
  }

  public DMPWithID convert(DmpDO dmp, org.damap.base.r3data.RepositoriesService repositoriesService) {
    return new DMPWithID().id(String.valueOf(dmp.getId())).dmp(convertData(dmp, repositoriesService));
  }

  public DmpDO convert(DMPWithID dmp) {
    var dmpDO = new DmpDO();
    dmpDO.setId(Long.valueOf(dmp.getId()));
    var dmpData = dmp.getDmp();
    if (dmpData == null) {
      return dmpDO;
    }
    convertData(dmpData, dmpDO);
    return dmpDO;
  }

  private DMPData convertData(DmpDO dmp, RepositoriesService repositoriesService) {
    DMPData result = new DMPData();
    result.setTitle(dmp.getTitle() != null ? dmp.getTitle() : "Untitled DMP");
    result.setDescription(dmp.getDescription());
    result.setRights(dmp.getDataRightsAndAccessControl());
    if (dmp.getCreated() != null) {
      result.setCreated(OffsetDateTime.ofInstant(dmp.getCreated().toInstant(), ZoneOffset.UTC));
    } else if (dmp.getModified() != null) {
      result.setCreated(OffsetDateTime.ofInstant(dmp.getModified().toInstant(), ZoneOffset.UTC));
    } else {
      result.setCreated(OffsetDateTime.now(ZoneOffset.UTC));
    }

    if (dmp.getModified() != null) {
      result.setModified(OffsetDateTime.ofInstant(dmp.getModified().toInstant(), ZoneOffset.UTC));
    } else if (dmp.getCreated() != null) {
      result.setModified(OffsetDateTime.ofInstant(dmp.getCreated().toInstant(), ZoneOffset.UTC));
    } else {
      result.setModified(OffsetDateTime.now(ZoneOffset.UTC));
    }
    result.setLanguage(LanguageCode.ENG);
    result.setDmpId(new DMPID().type("other").identifier(String.valueOf(dmp.getId())));

    ProjectDO project = dmp.getProject();
    if (project != null) {
      result.project(List.of(projectMapper.convert(project)));
    }
    if (dmp.getContact() != null) {
      result.setContact(contributorMapper.convertToContact(dmp.getContact()));
    } else {
      Contact placeholderContact = new Contact();
      placeholderContact.setName("Contact Not Provided");
      placeholderContact.setMbox("no-reply@example.com");
      placeholderContact.setContactId(new ContactID().identifier("not-provided").type("other"));
      result.setContact(placeholderContact);
    }
    var contributors = dmp.getContributors();
    if (contributors != null) {
      result.setContributor(
          contributors.stream().map(contributorMapper::convert).collect(Collectors.toList()));
    }
    var costs = dmp.getCosts();
    if (costs != null) {
      result.setCost(costs.stream().map(costsMapper::convert).collect(Collectors.toList()));
    }

    if (dmp.getDatasets() != null) {
      result.setDataset(dmp.getDatasets().stream().map(datasetMapper::convert).toList());

      for (int i = 0; i < dmp.getDatasets().size(); i++) {
        DatasetDO datasetDO = dmp.getDatasets().get(i);
        Dataset rdaDataset = result.getDataset().get(i);

        // Pass the repositoriesService to the helper
        List<Host> rdaHosts = findAllHostsForDataset(dmp, datasetDO, repositoriesService);

        if (!rdaHosts.isEmpty() && rdaDataset.getDistribution() != null && !rdaDataset.getDistribution().isEmpty()) {
          Distribution baseDist = rdaDataset.getDistribution().get(0);
          List<Distribution> distributions = new ArrayList<>();

          for (int h = 0; h < rdaHosts.size(); h++) {
            Host host = rdaHosts.get(h);
            if (h == 0) {
              baseDist.setHost(host);
              distributions.add(baseDist);
            } else {
              Distribution clone = cloneDistribution(baseDist);
              clone.setHost(host);
              distributions.add(clone);
            }
          }
          rdaDataset.setDistribution(distributions);
        }
      }
    } else {
      result.setDataset(new ArrayList<>());
    }

    var ethicalIssuesExist = dmp.getEthicalIssuesExist();
    if (ethicalIssuesExist != null) {
      if (ethicalIssuesExist) {
        result.setEthicalIssuesExist(Booleanish.YES);
      } else {
        result.setEthicalIssuesExist(Booleanish.NO);
      }
    } else {
      result.setEthicalIssuesExist(Booleanish.UNKNOWN);
    }
    result.setEthicalIssuesReport(dmp.getEthicalIssuesReport());
    return result;
  }

  private void convertData(DMPData data, DmpDO target) {
    target.setTitle(data.getTitle());
    target.setDescription(data.getDescription());
    target.setDataRightsAndAccessControl(data.getRights());
    var projects = data.getProject();
    if (projects != null) {
      if (projects.size() > 1 && strict) {
        throw new CommonStandardCompatibilityException("more than one project present");
      }
      String dmpIdString = data.getDmpId() != null ? data.getDmpId().getIdentifier() : null;
      for (var project : projects) {
        target.setProject(projectMapper.convert(project, dmpIdString));
      }
    }
    List<ContributorDO> damapContributors = new ArrayList<>();
    var contact = data.getContact();
    if (contact != null) {
      ContributorDO contactDO = contributorMapper.convertToContributor(contact);
      contactDO.setContact(true); // Flag this person as primary contact
      damapContributors.add(contactDO);
    }

    var contributors = data.getContributor();
    if (contributors != null) {
      damapContributors.addAll(
          contributors.stream().map(contributorMapper::convert).collect(Collectors.toList()));
    }

    target.setContributors(damapContributors);

    if (data.getLanguage() != LanguageCode.ENG && strict) {
      throw new CommonStandardCompatibilityException(
          "DAMAP does not support importing non-English DMPs");
    }
    var costs = data.getCost();
    if (costs != null) {
      target.setCosts(costs.stream().map(costsMapper::convert).toList());
    }
    var datasets = data.getDataset();
    if (datasets != null && !datasets.isEmpty()) {
      List<DatasetDO> damapDatasets = new ArrayList<>();
      for (var rdaDataset : datasets) {
        DatasetDO datasetDO = datasetMapper.convert(rdaDataset);

        if (datasetDO.getReferenceHash() == null) {
          datasetDO.setReferenceHash(java.util.UUID.randomUUID().toString());
        }
        damapDatasets.add(datasetDO);

        if (rdaDataset.getDistribution() != null && !rdaDataset.getDistribution().isEmpty()) {
          var distribution = rdaDataset.getDistribution().get(0);
          var rdaHost = distribution.getHost();
          if (rdaHost != null) {
            importHost(target, rdaHost, datasetDO.getReferenceHash());
          }
        }
      }

      target.setDatasets(damapDatasets);
      target.setDataKind(EDataKind.SPECIFY);
      target.setReusedDataKind(EDataKind.NONE);
    } else {
      target.setDatasets(List.of());
      target.setDataKind(EDataKind.NONE);
      target.setReusedDataKind(EDataKind.NONE);
    }
    if (data.getEthicalIssuesDescription() != null
        && !data.getEthicalIssuesDescription().isEmpty()
        && strict) {
      throw new CommonStandardCompatibilityException(
          "DAMAP does not support the ethical_issues_description field");
    }
    target.setEthicalIssuesExist(
        switch (data.getEthicalIssuesExist()) {
          case YES -> true;
          case NO -> false;
          case UNKNOWN -> null;
        });
    target.setEthicalIssuesReport(data.getEthicalIssuesReport());
  }

  private List<Host> findAllHostsForDataset(
          DmpDO dmp,
          DatasetDO datasetDO,
          org.damap.base.r3data.RepositoriesService repositoriesService) {

    List<Host> hosts = new ArrayList<>();
    if (datasetDO == null) {
      return hosts;
    }

    String refHash = datasetDO.getReferenceHash();
    String idStr = datasetDO.getId() != null ? String.valueOf(datasetDO.getId()) : null;

    // 1. Search Repositories (Dynamic URL Lookup!)
    if (dmp.getRepositories() != null) {
      for (var repo : dmp.getRepositories()) {
        if (repo.getDatasets() != null) {
          if ((refHash != null && repo.getDatasets().contains(refHash)) ||
                  (idStr != null && repo.getDatasets().contains(idStr))) {

            String repoUrl = "https://example.org/repository"; // Default Fallback

            System.out.println("DEBUG: Found matching Repository. Title: " + repo.getTitle() + " | re3data ID: " + repo.getRepositoryId());

            if (repo.getRepositoryId() != null && !repo.getRepositoryId().isBlank()) {
              try {
                var re3Data = repositoriesService.getById(repo.getRepositoryId());
                if (re3Data != null && !re3Data.getRepository().isEmpty()) {

                  // Get the repositoryURL from the schema
                  String url = re3Data.getRepository().get(0).getRepositoryURL();
                  System.out.println("DEBUG: Successfully fetched re3data URL: " + url);

                  if (url != null && !url.isBlank()) {
                    repoUrl = url;
                  }
                } else {
                  System.out.println("DEBUG: re3Data object is null or empty!");
                }
              } catch (Exception e) {
                System.err.println("DEBUG: Failed to fetch re3data URL for ID " + repo.getRepositoryId());
                e.printStackTrace(); // PRINT THE ACTUAL EXCEPTION TO LOGS
              }
            } else {
              System.out.println("DEBUG: Skipping re3data lookup because repositoryId is null/blank!");
            }

            hosts.add(new Host()
                    .title(repo.getTitle() != null ? repo.getTitle() : "Repository")
                    .url(repoUrl));
          }
        }
      }
    }

    // 2. Search External Storage
    if (dmp.getExternalStorage() != null) {
      for (var ext : dmp.getExternalStorage()) {
        if (ext.getDatasets() != null) {
          if ((refHash != null && ext.getDatasets().contains(refHash)) ||
                  (idStr != null && ext.getDatasets().contains(idStr))) {
            hosts.add(new Host()
                    .title(ext.getTitle() != null ? ext.getTitle() : "External Storage")
                    .url(ext.getUrl() != null && !ext.getUrl().isBlank() ? ext.getUrl() : "https://example.org/external-storage"));
          }
        }
      }
    }

    // 3. Search Storage
    if (dmp.getStorage() != null) {
      for (var store : dmp.getStorage()) {
        if (store.getDatasets() != null) {
          if ((refHash != null && store.getDatasets().contains(refHash)) ||
                  (idStr != null && store.getDatasets().contains(idStr))) {
            hosts.add(new Host()
                    .title(store.getTitle() != null ? store.getTitle() : "Storage")
                    .url("https://example.org/storage"));
          }
        }
      }
    }


    //  4. Fallback if empty and only 1 dataset + 1 host
    if (hosts.isEmpty() && dmp.getDatasets() != null && dmp.getDatasets().size() == 1) {
      if (dmp.getRepositories() != null && dmp.getRepositories().size() == 1) {
        var repo = dmp.getRepositories().get(0);
        String repoUrl = "https://example.org/repository";
        if (repo.getRepositoryId() != null) {
          try {
            var re3data = repositoriesService.getById(repo.getRepositoryId());
            if (re3data != null && !re3data.getRepository().isEmpty()) {
              String url = re3data.getRepository().get(0).getRepositoryURL();
              if (url != null && !url.isBlank()) {
                repoUrl = url;
              }
            }
          } catch (Exception ignored) {}
        }
        hosts.add(new Host().title(repo.getTitle()).url(repoUrl));
      } else if (dmp.getExternalStorage() != null && dmp.getExternalStorage().size() == 1) {
        var ext = dmp.getExternalStorage().get(0);
        hosts.add(new Host().title(ext.getTitle()).url(ext.getUrl() != null ? ext.getUrl() : "https://example.org/external-storage"));
      } else if (dmp.getStorage() != null && dmp.getStorage().size() == 1) {
        var store = dmp.getStorage().get(0);
        hosts.add(new Host().title(store.getTitle()).url("https://example.org/storage"));
      }
    }

    return hosts;
  }

  private Distribution cloneDistribution(Distribution source) {
    Distribution target = new Distribution();
    target.setTitle(source.getTitle());
    target.setDataAccess(source.getDataAccess());
    target.setByteSize(source.getByteSize());
    target.setFormat(source.getFormat());
    target.setLicense(source.getLicense());
    return target;
  }

  private void importHost(DmpDO target, Host rdaHost, String refHash) {
    String title = rdaHost.getTitle() != null && !rdaHost.getTitle().isBlank()
            ? rdaHost.getTitle()
            : "Imported Host";
    String url = rdaHost.getUrl();

    if (url != null && !url.isBlank()) {
      if (target.getExternalStorage() == null) {
        target.setExternalStorage(new ArrayList<>());
      }

      ExternalStorageDO existingExt = null;
      for (var ext : target.getExternalStorage()) {
        if (title.equalsIgnoreCase(ext.getTitle())) {
          existingExt = ext;
          break;
        }
      }

      if (existingExt != null) {
        if (!existingExt.getDatasets().contains(refHash)) {
          existingExt.getDatasets().add(refHash);
        }
      } else {
        var newExt = new ExternalStorageDO();
        newExt.setTitle(title);
        newExt.setUrl(url);
        newExt.setDatasets(new ArrayList<>(List.of(refHash)));
        target.getExternalStorage().add(newExt);
      }
    } else {
      if (target.getRepositories() == null) {
        target.setRepositories(new ArrayList<>());
      }

      RepositoryDO existingRepo = null;
      for (var repo : target.getRepositories()) {
        if (title.equalsIgnoreCase(repo.getTitle())) {
          existingRepo = repo;
          break;
        }
      }

      if (existingRepo != null) {
        if (!existingRepo.getDatasets().contains(refHash)) {
          existingRepo.getDatasets().add(refHash);
        }
      } else {
        var newRepo = new RepositoryDO();
        newRepo.setTitle(title);
        newRepo.setDatasets(new ArrayList<>(List.of(refHash)));
        target.getRepositories().add(newRepo);
      }
    }
  }
}
