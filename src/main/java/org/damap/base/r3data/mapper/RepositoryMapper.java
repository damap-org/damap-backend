package org.damap.base.r3data.mapper;

import static org.damap.base.utils.EqualityUtils.nullExclusiveEquals;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.r3data.dto.RepositoryDetails;
import org.re3data.schema._2_2.Languages;
import org.re3data.schema._2_2.PidSystems;
import org.re3data.schema._2_2.Re3Data;
import org.re3data.schema._2_2.Yesno;

/** RepositoryMapper class. */
@UtilityClass
public class RepositoryMapper {

  /**
   * mapToRepositoryDetails.
   *
   * @param re3Data a {@link org.re3data.schema._2_2.Re3Data} object
   * @param id a {@link java.lang.String} object
   * @return a {@link org.damap.base.r3data.dto.RepositoryDetails} object
   */
  public RepositoryDetails mapToRepositoryDetails(Re3Data re3Data, String id) {
    RepositoryDetails repositoryDetails = new RepositoryDetails();

    if (re3Data.getRepository().isEmpty()) {
      return repositoryDetails;
    }

    Re3Data.Repository repo = re3Data.getRepository().get(0);
    repositoryDetails.setId(id);
    repositoryDetails.setName(repo.getRepositoryName().getValue());
    repositoryDetails.setRepositoryURL(repo.getRepositoryURL());
    repositoryDetails.setDescription(repo.getDescription().getValue());
    repositoryDetails.setVersioning(mapYesNoToBoolean(repo.getVersioning()));
    repositoryDetails.setRepositoryIdentifier(repo.getRepositoryIdentifier());

    if (!repo.getRepositoryLanguage().isEmpty()) {
      ArrayList<String> languages = new ArrayList<>();
      for (Languages lang : repo.getRepositoryLanguage()) {
        languages.add(lang.value());
      }
      repositoryDetails.setRepositoryLanguages(languages);
    }

    if (!repo.getMetadataStandard().isEmpty()) {
      ArrayList<String> metadata = new ArrayList<>();
      for (Re3Data.Repository.MetadataStandard mds : repo.getMetadataStandard()) {
        metadata.add(mds.getMetadataStandardName().getValue().value());
      }
      repositoryDetails.setMetadataStandards(metadata);
    }

    if (!repo.getContentType().isEmpty()) {
      ArrayList<String> types = new ArrayList<>();
      for (Re3Data.Repository.ContentType ct : repo.getContentType()) {
        types.add(ct.getValue().value());
      }
      repositoryDetails.setContentTypes(types);
    }

    List<PidSystems> pidSystems = repo.getPidSystem();
    List<EIdentifierType> pidIdentifiers = new ArrayList<>();
    for (PidSystems pidSystem : pidSystems) {
      if (!nullExclusiveEquals(pidSystem.name(), ("NONE"))) {
        pidIdentifiers.add(EIdentifierType.valueOf(pidSystem.name()));
      }
    }
    repositoryDetails.setPidSystems(pidIdentifiers);

    return repositoryDetails;
  }

  /**
   * mapYesNoToBoolean.
   *
   * @param value a {@link org.re3data.schema._2_2.Yesno} object
   * @return a {@link java.lang.Boolean} object
   */
  @jakarta.annotation.Nullable public Boolean mapYesNoToBoolean(Yesno value) {
    if (value == null) {
      return null;
    }
    return nullExclusiveEquals(value.value(), Yesno.YES.value());
  }
}
