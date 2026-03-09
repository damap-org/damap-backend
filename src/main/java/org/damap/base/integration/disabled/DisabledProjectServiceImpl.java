package org.damap.base.integration.disabled;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;

/**
 * This class is meant to express the state of project services being explicitly disabled.
 *
 * <p>Note: DAMAP was coded in a way, where it was assumed that a project service would always
 * exist. This husk is meant to represent the state of no service being registered. This should be
 * refactored in the future, if time permits.
 */
@ApplicationScoped
public class DisabledProjectServiceImpl implements ProjectServiceProvider {
  @Override
  public String getConfigID() {
    return "disabled";
  }

  @Override
  public List<ContributorDO> getProjectStaff(String projectId) {
    return new ArrayList<>();
  }

  @Override
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    return null;
  }

  @Override
  public ContributorDO getProjectLeader(String projectId) {
    return null;
  }

  @Override
  public ResultList<ProjectDO> getRecommended(Search search) {
    return getMockResultList();
  }

  @Override
  public ProjectDO read(String id) {
    return null;
  }

  @Override
  public ResultList<ProjectDO> search(Search query) {
    return getMockResultList();
  }

  /**
   * Helper method. Returns an empty ResultList that won't crash methods which use the result, since
   * they expect empty lists and not null.
   *
   * @return Empty result list
   */
  private ResultList<ProjectDO> getMockResultList() {
    ResultList<ProjectDO> res = new ResultList<>();
    res.setItems(new ArrayList<>());
    return res;
  }
}
