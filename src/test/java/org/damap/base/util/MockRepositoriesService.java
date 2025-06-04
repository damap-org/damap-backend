package org.damap.base.util;

import io.quarkus.test.Mock;
import java.util.ArrayList;
import java.util.List;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.r3data.RepositoriesService;
import org.damap.base.r3data.dto.RepositoryDetails;
import org.damap.base.r3data.mapper.RepositoryMapper;
import org.re3data.schema._2_2.*;

@Mock
public class MockRepositoriesService extends RepositoriesService {

  private Re3Data getMockR3DataResponse() {
    Re3Data r3data = new Re3Data();
    r3data.getRepository().add(new Re3Data.Repository());
    Re3Data.Repository repo = r3data.getRepository().get(0);

    Re3Data.Repository.RepositoryName repoName = new Re3Data.Repository.RepositoryName();
    repoName.setValue("Cool Repository");
    repo.setRepositoryName(repoName);
    repo.setRepositoryURL("https://zenodo.org/");
    Re3Data.Repository.Description description = new Re3Data.Repository.Description();
    description.setValue("Very Cool Repository");
    repo.setDescription(description);
    repo.setVersioning(Yesno.YES);
    repo.getRepositoryIdentifier().add("identifier");
    repo.getRepositoryLanguage().add(Languages.ENG);
    Re3Data.Repository.MetadataStandard metadataStandard =
        new Re3Data.Repository.MetadataStandard();
    Re3Data.Repository.MetadataStandard.MetadataStandardName metadataStandardName =
        new Re3Data.Repository.MetadataStandard.MetadataStandardName();
    metadataStandardName.setValue(MetadataStandardDCCNames.DARWIN_CORE);
    metadataStandard.setMetadataStandardName(metadataStandardName);
    repo.getMetadataStandard().add(metadataStandard);
    Re3Data.Repository.ContentType contentType = new Re3Data.Repository.ContentType();
    contentType.setValue(ContentTypeText.IMAGES);
    repo.getContentType().add(contentType);
    repo.getPidSystem().add(PidSystems.DOI);

    return r3data;
  }

  public List<RepositoryDetails> getRecommended() {
    List<RepositoryDetails> recommendedRepositories = new ArrayList<>();
    Re3Data repo = this.getMockR3DataResponse();
    recommendedRepositories.add(RepositoryMapper.mapToRepositoryDetails(repo, "r3d100010468"));
    return recommendedRepositories;
  }

  /**
   * getById.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link org.re3data.schema._2_2.Re3Data} object
   */
  public Re3Data getById(String id) {
    return getMockR3DataResponse();
  }

  /**
   * getDescription.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   */
  public String getDescription(String id) {
    return RepositoryMapper.mapToRepositoryDetails(getMockR3DataResponse(), id).getDescription();
  }

  /**
   * getRepositoryURL.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   */
  public String getRepositoryURL(String id) {
    return RepositoryMapper.mapToRepositoryDetails(getMockR3DataResponse(), id).getRepositoryURL();
  }

  /**
   * getPidSystems.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  public List<EIdentifierType> getPidSystems(String id) {
    return RepositoryMapper.mapToRepositoryDetails(getMockR3DataResponse(), id).getPidSystems();
  }
}
