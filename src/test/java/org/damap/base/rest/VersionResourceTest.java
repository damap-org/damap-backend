package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.TestProfiles;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.version.VersionDO;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(VersionResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class VersionResourceTest {

  @Inject TestDOFactory testDOFactory;

  @InjectMock SecurityService securityService;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.getUserId()).thenReturn("012345");
    Mockito.when(securityService.getUserName()).thenReturn("testUser");
  }

  @Test
  void testGetDmpVersionsEndpoint_Unauthorized() {
    given().when().get("/list/0").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetDmpVersionsEndpoint_Valid() {
    VersionDO versionDO = testDOFactory.getOrCreateTestVersionDO();
    given().when().get("/list/" + versionDO.getDmpId()).then().statusCode(200);
  }

  @Test
  void testSaveDmpVersionsEndpoint_Unauthorized() {
    VersionDO versionDO = new VersionDO();
    versionDO.setVersionName("TestVersion");
    versionDO.setDmpId(0L);
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(versionDO)
        .when()
        .put()
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSaveDmpVersionsEndpoint_Valid() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    VersionDO versionDO = new VersionDO();
    versionDO.setVersionName("TestVersion2");
    versionDO.setDmpId(dmpDO.getId());
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(versionDO)
        .when()
        .put()
        .then()
        .statusCode(200)
        .body("id", notNullValue());
  }
}
