package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.version.VersionService;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(DataManagementPlanResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class DataManagementPlanResourceTest extends TestSetup {

  @Inject TestDOFactory testDOFactory;

  @Inject VersionService versionService;

  @Inject SecurityService securityService;

  @Test
  void testGetAllPlansEndpoint_Invalid() {
    given().when().get("/all").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetAllPlansEndpoint_InvalidRole() {
    given().when().get("/all").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testGetAllPlansEndpoint_ValidRole() {
    given().when().get("/all").then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetPlansEndpoint_Valid() {
    given().when().get("/list").then().statusCode(200);
  }

  @Test
  void testGetPlansEndpoint_Invalid() {
    given().when().get("/list").then().statusCode(401);
  }

  @Test
  void testPlanByIdEndpoint_Invalid() {
    given().when().get("/1").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testPlanByIdEndpoint_Valid() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    given().when().get("/" + dmpDO.getId()).then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testPlanByIdEndpoint_Unauthorized() {
    given().when().get("/0").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSavePlanEndpoint_Valid() {
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createDmpDO())
        .when()
        .post("")
        .then()
        .statusCode(200)
        .body("title", is("Mock Dmp"));
  }

  @Test
  void testSavePlanEndpoint_Invalid() {
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createDmpDO())
        .when()
        .post("")
        .then()
        .statusCode(401);
  }

  @Test
  void testUpdatePlanEndpoint_Invalid() {
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .when()
        .put("/1")
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testUpdatePlanEndpoint_Valid() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDOEmpty();
    dmpDO.setMetadata("Different String for metadata");
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(dmpDO)
        .when()
        .put("/" + dmpDO.getId())
        .then()
        .statusCode(200)
        .body("title", is("EmptyTestDmp"));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testUpdatePlanEndpoint_Unauthorized() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDOEmpty();
    dmpDO.setMetadata("Different String for metadata");
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(dmpDO)
        .when()
        .put("/0")
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testDeletePlanOwner_Valid() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    given().when().delete("/" + dmpDO.getId()).then().statusCode(204);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testDeletePlan_Invalid() {
    // use unauthorized user id
    Mockito.when(securityService.getUserId()).thenReturn("12345");
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    given().when().delete("/" + dmpDO.getId()).then().statusCode(403);
  }

  @Test
  void testGetDmpByIdAndRevisionlanEndpoint_Invalid() {
    given().contentType(MediaType.APPLICATION_JSON).when().get("/0/1").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetDmpByIdAndRevisionlanEndpoint_Unauthorized() {
    given().contentType(MediaType.APPLICATION_JSON).when().get("/0/1").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetDmpByIdAndRevisionlanEndpoint_Valid() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    testDOFactory.getOrCreateTestVersionDO();
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .when()
        .get(
            "/"
                + dmpDO.getId()
                + "/"
                + versionService.getDmpVersions(dmpDO.getId()).get(0).getRevisionNumber())
        .then()
        .statusCode(200);
  }

  private DmpDO createDmpDO() {
    DmpDO dmpDO = new DmpDO();
    dmpDO.setTitle("Mock Dmp");
    return dmpDO;
  }
}
