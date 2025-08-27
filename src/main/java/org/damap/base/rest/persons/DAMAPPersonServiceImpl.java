package org.damap.base.rest.persons;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.damap.base.domain.User;
import org.damap.base.integration.PersonService;
import org.damap.base.repo.UserRepo;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;

@ApplicationScoped
@Priority(-1)
@Slf4j
public class DAMAPPersonServiceImpl implements PersonService {

  @Inject UserRepo userRepo;

  @Override
  public ContributorDO read(String id) {
    return PersonService.super.read(id);
  }

  @Override
  public ResultList<ContributorDO> search(Search query) {
    log.info("Search persons with query: {}", query);
    List<User> users = userRepo.searchByNickname(query.getQuery());
    List<ContributorDO> contributors =
        users.stream()
            .map(
                user -> {
                  ContributorDO contributorDO = new ContributorDO();
                  log.info("Mapping user: {}", user);
                  contributorDO.setUniversityId(user.getSubject());
                  String affiliation =
                      user.getSubject().contains("@") ? user.getSubject().split("@")[1] : "unknown";
                  contributorDO.setAffiliation(affiliation);
                  contributorDO.setMbox(user.getEmail());
                  String[] names = user.getNickname().split(" ");
                  contributorDO.setFirstName(names[0]);
                  contributorDO.setLastName(
                      names.length > 1
                          ? String.join(" ", Arrays.copyOfRange(names, 1, names.length))
                          : "");
                  return contributorDO;
                })
            .toList();

    return ResultList.fromItemsAndSearch(contributors, query);
  }
}
