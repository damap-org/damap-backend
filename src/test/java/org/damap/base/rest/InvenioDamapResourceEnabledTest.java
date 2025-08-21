package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.domain.Dmp;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.integration.mock.MockUniversityPersonServiceImpl;
import org.damap.base.integration.orcid.ORCIDMapper;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.invenio_damap.DMPPayload;
import org.damap.base.rest.invenio_damap.InvenioDAMAPResource;
import org.damap.base.rest.madmp.dto.Dataset;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// NOTE:
// We could put tests for the same resource into one file and annotate sub classes with @Nested.
// However, for some reason, this is really slow (3 Tests took 60 seconds).

@QuarkusTest
@TestProfile(TestProfiles.InvenioDAMAPEnabledProfile.class)
@TestHTTPEndpoint(InvenioDAMAPResource.class)
class InvenioDamapResourceEnabledTest {

  @ConfigProperty(name = "invenio.shared-secret")
  String sharedSecret;

  @Inject TestDOFactory testDOFactory;

  @Inject DmpRepo dmpRepo;

  @Inject DmpService dmpService;
  @InjectMock MockUniversityPersonServiceImpl personService;

  @InjectMock ORCIDPersonServiceImpl orcidPersonServiceImpl;

  @InjectMock
  MockProjectServiceImpl projectService;



  @BeforeEach
  public void setup() {
    Mockito.when(personService.read(any(String.class)))
            .thenReturn(testDOFactory.getTestContributorDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
            .thenReturn(ORCIDMapper.mapRecordEntityToPersonDO(testDOFactory.getORCIDTestRecord()));
    Mockito.when(projectService.getProjectLeader(any()))
            .thenReturn(testDOFactory.getTestContributorDO());
  }
  @BeforeEach
  public void cleanup() {
    List<Dmp> dmps = dmpRepo.getAll();
    for (Dmp dmp : dmps) dmpService.delete(dmp.id);
  }

  private JsonObject createInvenioDamapClaim(String userId) {
    return Json.createObjectBuilder()
        .add("identifiers", Json.createObjectBuilder().add("personID", userId))
        .build();
  }

  private String generateJwt(long expiresIn) {
    long currentTime = System.currentTimeMillis() / 1000;
    long exp = currentTime + expiresIn;
    String userId = getUserId();
    JwtClaimsBuilder claimsBuilder = Jwt.claims();

    claimsBuilder
        .subject(userId)
        .expiresAt(exp)
        .issuedAt(currentTime)
        .claim("invenio-damap", createInvenioDamapClaim(userId));

    return claimsBuilder.signWithSecret(sharedSecret);
  }

  private String getUserId() {
    return "012345";
  }

  @Test
  void getDmpsForPersonThenUnauthorized() {
    // case where no header is passed
    given().when().get().then().statusCode(401);

    // case where empty value is included in the header
    given().when().header("X-Auth", "").get().then().statusCode(401);

    // case where wrong token is included in the header
    given().when().header("X-Auth", "wrong-credentials").get().then().statusCode(401);

    // case where token has expired
    String jwtToken = generateJwt(0);
    given().when().header("X-Auth", jwtToken).get().then().statusCode(401);
  }

  @Test
  void getDmpsForPersonThenValid() {
    Header authHeader = new Header("X-Auth", generateJwt(600));

    // case where user has no DMPs (which means identity is not present in the
    // system or identity is invalid)
    given().when().header(authHeader).get().then().statusCode(401);

    // case with DMP created
    DmpDO dmpDO = testDOFactory.createDmp("invenio-damap", true);
    given()
        .when()
        .header(authHeader)
        .get()
        .then()
        .statusCode(200)
        .body("", hasSize(1))
        .body("[0].title", equalTo(dmpDO.getTitle()));
  }

  @Test
  void addDataSetToDmpThenValid() throws JsonProcessingException {
    Header authHeader = new Header("X-Auth", generateJwt(600));
    String datasetData =
        """
            {
            "dataset_id" : {
                "type": "doi",
                "identifier": "repository/123.456"
            },
            "description" : "Dataset description",
            "distribution" : [ {
            "access_url" : "https://repository.tugraz.at/",
            "byte_size" : 705032704,
            "data_access" : "open",
            "description" : "Distribution description",
            "format" : [ "Standard office documents", "Images", "Raw data", "Scientific and statistical data formats", "Plain text" ],
            "host" : {
                "description" : "An institutional repository at Graz University of Technology to enable storing, sharing and publishing research data, publications and open educational resources. It provides open access services and follows the FAIR principles.",
                "pid_system" : [ "doi" ],
                "storage_type" : "institutional",
                "support_versioning" : "yes",
                "title" : "TU Graz Repository",
                "url" : "https://repository.tugraz.at/"
            },
            "license" : [ {
                "license_ref" : "https://creativecommons.org/licenses/by-nc-sa/4.0/"
            } ],
            "title" : "Host title"
            } ],
            "personal_data" : "no",
            "security_and_privacy" : [ ],
            "sensitive_data" : "no",
            "title" : "Dataset title",
            "type" : "Standard office documents, Images, Raw data, Scientific and statistical data formats, Plain text"
        }""";

    DmpDO dmpDO = testDOFactory.createDmp("invenio-damap", true);
    Dataset madmpDataset = (new ObjectMapper()).readValue(datasetData, Dataset.class);

    DMPPayload payload1 = new DMPPayload();
    payload1.setDmpId(dmpDO.getId());
    payload1.setDataset(madmpDataset);

    // case with dataset creation
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(authHeader)
        .body(payload1)
        .post()
        .then()
        .statusCode(200)
        .body("", not(empty()))
        .body("datasets", hasSize(3))
        .body("datasets[2].title", equalTo(madmpDataset.getTitle()));

    Dataset minimalDataset = new Dataset();
    minimalDataset.setTitle("minimal");

    DMPPayload payload2 = new DMPPayload();
    payload2.setDmpId(dmpDO.getId());
    payload2.setDataset(minimalDataset);

    // case with minimal dataset creation
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(authHeader)
        .body(payload2)
        .post()
        .then()
        .statusCode(200)
        .body("", not(empty()))
        .body("datasets", hasSize(4))
        .body("datasets[3].title", equalTo(minimalDataset.getTitle()));
  }
}
