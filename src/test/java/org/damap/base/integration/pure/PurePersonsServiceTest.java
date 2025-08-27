package org.damap.base.integration.pure;

import com.google.common.io.Resources;
import io.quarkus.arc.All;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.List;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.integration.PersonService;
import org.damap.base.rest.base.Pagination;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(PureTestProfile.class)
public class PurePersonsServiceTest {
  private PersonService createPersonsService() {
    FileBasedPureAPI api = new FileBasedPureAPI();
    api.personsFile = Resources.getResource("org/damap/base/integration/pure/persons.json");
    api.projectsFile = Resources.getResource("org/damap/base/integration/pure/projects.json");

    PurePersonService personService = new PurePersonService();
    personService.pureAPI = api;
    return personService;
  }

  private ContributorDO getFirstPerson() {
    ContributorDO expected = new ContributorDO();
    IdentifierDO id = new IdentifierDO();
    id.setType(EIdentifierType.ORCID);
    id.setIdentifier("0000-0001-2345-6789");
    expected.setPersonId(id);
    expected.setFirstName("Jane");
    expected.setLastName("Doe");
    expected.setMbox("jane.doe@example.com");
    expected.setUniversityId("a8864c16-5264-4d94-955d-8be7e00f26a9");
    return expected;
  }

  private ContributorDO getSecondPerson() {
    ContributorDO expected = new ContributorDO();
    IdentifierDO id = new IdentifierDO();
    id.setType(EIdentifierType.ORCID);
    id.setIdentifier("0001-0001-2345-6789");
    expected.setPersonId(id);
    expected.setFirstName("John");
    expected.setLastName("Doe");
    expected.setMbox("john.doe@example.com");
    expected.setUniversityId("72993125-c1b7-4b14-a111-c36a72d59da0");
    return expected;
  }

  @Test
  public void testRead() {
    PersonService svc = createPersonsService();

    ContributorDO expected = getFirstPerson();
    Assertions.assertEquals(expected, svc.read(expected.getUniversityId()));

    expected = getSecondPerson();
    Assertions.assertEquals(expected, svc.read(expected.getUniversityId()));

    // This ID doesn't exist.
    Assertions.assertNull(svc.read("39209262-4832-4008-913c-cbd6b2d56187"));
  }

  @Test
  public void testSearch() {
    PersonService svc = createPersonsService();

    ContributorDO expected = getSecondPerson();
    Search search = new Search();
    search.setQuery(expected.getFirstName().substring(0, 3));
    search.setPagination(new Pagination());
    ResultList<ContributorDO> results = svc.search(search);
    Assertions.assertEquals(1, results.getItems().size());
    Assertions.assertEquals(expected, results.getItems().get(0));
  }

  @Inject PureAPI pureAPI;

  @Inject @All List<PersonService> personsServices;

  @Test
  public void testWiring() {
    Assertions.assertNotNull(pureAPI);
    Assertions.assertTrue(pureAPI instanceof FileBasedPureAPI);

    PersonService svc =
        personsServices.stream()
            .filter(item -> item instanceof PurePersonService)
            .findFirst()
            .orElse(null);
    Assertions.assertNotNull(svc);

    ContributorDO expected = getFirstPerson();
    Assertions.assertEquals(expected, svc.read(expected.getUniversityId()));
  }
}
