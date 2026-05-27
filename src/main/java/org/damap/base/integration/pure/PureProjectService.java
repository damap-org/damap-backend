package org.damap.base.integration.pure;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.config.domain.DamapTenantAwareConfig;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;
import org.damap.base.security.SecurityService;

/**
 * This class partially implements reading Elsevier Pure Project and Person
 * objects from their API.
 *
 * <p>
 * <strong>Note:</strong> this implementation is currently experimental.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure
 *      API doc</a>
 */
@JBossLog
@ApplicationScoped
public class PureProjectService implements ProjectServiceProvider {

    @Inject
    PureAPI pureAPI;
    @Inject
    SecurityService securityService;

    @Inject
    TenantConfigResolver tenantConfigResolver;

    @Override
    public List<ContributorDO> getProjectStaff(String projectId) {
        PureAPIProject project = pureAPI.getProject(projectId);
        if (project == null) {
            return null;
        }
        if (project.participants == null) {
            return new ArrayList<>();
        }
        // TODO what if multiple associations are present for one person?
        return project.participants.stream().map(this::getContributorDO).toList();
    }

    private ContributorDO getContributorDO(PureAPIParticipantAssociation participantAssociation) {
        if (participantAssociation instanceof PureAPIInternalParticipantAssociation internal) {
            ContributorDO contributor = pureAPI.getPerson(internal.person.uuid).toContributor();
            convertContributorRoles(participantAssociation, contributor);
            return contributor;
        }
        if (participantAssociation instanceof PureAPIExternalParticipantAssociation external) {
            ContributorDO contributor = new ContributorDO();
            if (external.name != null) {
                contributor.setFirstName(external.name.firstName);
                contributor.setLastName(external.name.lastName);
            }
            convertContributorRoles(participantAssociation, contributor);
            if (external.externalPerson != null) {
                contributor.setUniversityId(external.externalPerson.uuid);
            }
            return contributor;
        }
        return null;
    }

    private void convertContributorRoles(
            PureAPIParticipantAssociation participantAssociation, ContributorDO contributor) {
        List<DamapTenantAwareConfig.PureRoleClassification> elsevierPureContributorRoleClassifications = tenantConfigResolver
                .getTenantAwareConfig().elsevierPureContributorRoleClassifications();
        if (participantAssociation.role != null && participantAssociation.role.uri != null) {
            elsevierPureContributorRoleClassifications.stream()
                    .filter(
                            pureRoleClassification -> pureRoleClassification.pureRoleUri()
                                    .equals(participantAssociation.role.uri))
                    .findFirst()
                    .map(DamapTenantAwareConfig.PureRoleClassification::contributorRole)
                    .ifPresent(role -> contributor.setRoles(Set.of(role)));
        }
    }

    @Override
    public String getConfigID() {
        return "elsevier-pure";
    }

    @Override
    public ContributorDO getProjectLeader(String projectId) {
        PureAPIProject project = pureAPI.getProject(projectId);
        if (project == null || project.participants == null) {
            return null;
        }
        return project.participants.stream()
                .filter(participantAssociation -> participantAssociation.role != null)
                .filter(participantAssociation -> participantAssociation.role.uri != null)
                .filter(
                        participantAssociation -> participantAssociation.role.uri.equals(
                                tenantConfigResolver
                                        .getTenantAwareConfig()
                                        .elsevierPureProjectLeadRoleClassification()))
                .map(this::getContributorDO)
                .findFirst()
                .orElse(null);
    }

    @Override
    public ProjectSupplementDO getProjectSupplement(String projectId) {
        return new ProjectSupplementDO();
    }

    @Override
    // the cache key generator function uses the method name - take care when
    // renaming
    @CacheResult(cacheName = "pure-recommended", keyGenerator = PureCacheKeyGenerator.class)
    public ResultList<ProjectDO> getRecommended(Search search) {
        ResultList<ProjectDO> res = new ResultList<>();
        res.setSearch(search);

        String userId = securityService.getUserId();
        if (userId == null) {
            res.setItems(new ArrayList<>());
            return res;
        }

        List<ProjectDO> matchingProjects = pureAPI.listAllProjects().stream()
                .filter(project -> project.participants != null)
                .filter(
                        project -> project.participants.stream()
                                // Filter by internal participants only, since external participants are
                                // not able to have a DAMAP account now and for the foreseeable future.
                                .filter(
                                        participant -> participant instanceof PureAPIInternalParticipantAssociation)
                                .anyMatch(
                                        participant -> {
                                            PureAPIInternalParticipantAssociation internal = (PureAPIInternalParticipantAssociation) participant;
                                            return internal.person != null
                                                    && internal.person.uuid != null
                                                    && internal.person.uuid.equals(userId);
                                        }))
                .map(
                        project -> project.toProjectDO(
                                tenantConfigResolver
                                        .getTenantAwareConfig()
                                        .elsevierPureDescriptionClassification()))
                .toList();

        res.setItems(matchingProjects);

        return res;
    }

    @Override
    @CacheResult(cacheName = "pure-read-project", keyGenerator = PureCacheKeyGenerator.class)
    public ProjectDO read(String id) {
        PureAPIProject project = pureAPI.getProject(id);
        if (project == null || project.participants == null) {
            return null;
        }
        return project.toProjectDO(
                tenantConfigResolver.getTenantAwareConfig().elsevierPureDescriptionClassification());
    }

    @Override
    @CacheResult(cacheName = "pure-search-projects", keyGenerator = PureCacheKeyGenerator.class)
    public ResultList<ProjectDO> search(Search query) {
        ResultList<ProjectDO> res = new ResultList<>();
        res.setSearch(query);
        res.setItems(
                pureAPI.searchAllProjects(query.getQuery()).stream()
                        .map(
                                project -> project.toProjectDO(
                                        tenantConfigResolver
                                                .getTenantAwareConfig()
                                                .elsevierPureDescriptionClassification()))
                        .toList());
        return res;
    }
}
