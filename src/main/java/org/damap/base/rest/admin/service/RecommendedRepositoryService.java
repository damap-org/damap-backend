package org.damap.base.rest.admin.service;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.RecommendedRepository;
import org.damap.base.r3data.RepositoriesService;
import org.damap.base.rest.admin.domain.RecommendedRepositoryDO;
import org.re3data.schema._2_2.Re3Data;

@ApplicationScoped
@JBossLog
public class RecommendedRepositoryService {

  @Inject RepositoriesService repositoriesService;

  private boolean isValidRepositoryId(String repositoryId) {
    if (repositoryId == null) {
      return false;
    }
    if (!repositoryId.matches("^r3d\\d{6,}$")) { // not the official pattern
      return false;
    }
    try {
      repositoriesService.getById(repositoryId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public List<RecommendedRepositoryDO> getRecommendedRepositoriesWithNames() {
    List<RecommendedRepository> recommendedRepositories =
        RecommendedRepository.listAll(Sort.by("id").ascending());
    List<RecommendedRepositoryDO> result = new ArrayList<>();

    for (RecommendedRepository repo : recommendedRepositories) {
      RecommendedRepositoryDO dto = new RecommendedRepositoryDO();
      dto.setId(repo.id);
      dto.setRepositoryId(repo.getRepositoryId());

      try {
        Re3Data re3Data = repositoriesService.getById(repo.getRepositoryId());
        if (!re3Data.getRepository().isEmpty()) {
          dto.setName(re3Data.getRepository().get(0).getRepositoryName().getValue());
        }
      } catch (Exception e) {
        log.infov(
            "Failed to retrieve repository name for ID {0}, error: {1}",
            repo.getRepositoryId(), e.getMessage());
      }

      result.add(dto);
    }

    return result;
  }

  @Transactional
  public RecommendedRepository createRecommendedRepository(
      @Valid RecommendedRepository recommendedRepository) {
    RecommendedRepository existing =
        RecommendedRepository.find("repositoryId = ?1", recommendedRepository.getRepositoryId())
            .firstResult();

    if (existing != null) {
      throw new ClientErrorException(
          "Repository with repository ID '"
              + recommendedRepository.getRepositoryId()
              + "' already exists",
          Response.Status.CONFLICT);
    }

    // validate and only allow via Re3Data IDs
    if (!isValidRepositoryId(recommendedRepository.getRepositoryId())) {
      throw new BadRequestException(
          "Invalid or unknown Re3Data repository ID: " + recommendedRepository.getRepositoryId());
    }

    recommendedRepository.persist();

    return recommendedRepository;
  }

  @Transactional
  public void deleteRecommendedRepository(Long id) {
    RecommendedRepository repository = RecommendedRepository.findById(id);
    if (repository == null) {
      throw new NotFoundException("Repository with database ID '" + id + "' does not exist");
    }

    repository.delete();
  }
}
