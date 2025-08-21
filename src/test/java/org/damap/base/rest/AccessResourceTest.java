package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.enums.EFunctionRole;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.rest.access.service.AccessService;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(AccessResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class AccessResourceTest extends TestSetup {
  @Inject AccessService accessService;

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetAccessList_Valid() {
    given()
        .when()
        .get("/dmps/" + dmpDO.getId())
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testGetAccessListAdmin_Valid() {
    Mockito.when(securityService.isAdmin()).thenReturn(true);

    given()
        .when()
        .get("/dmps/" + dmpDO.getId())
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetAccessListDmpIsNull_Invalid() {
    given().when().get("/dmps/-1").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testCreateAccess_Valid() {
    AccessDO accessDO = this.createAccessDO();

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(accessDO)
        .when()
        .post("/")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testCreateAccessAdmin_Valid() {
    Mockito.when(securityService.isAdmin()).thenReturn(true);
    AccessDO accessDO = this.createAccessDO();

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(accessDO)
        .when()
        .post("/")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testCreateAccessNoContributor_InValid() {
    AccessDO accessDO = this.createAccessDO();
    accessDO.setUniversityId("234567");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(accessDO)
        .when()
        .post("/")
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testCreateAccessNoPermission_Invalid() {
    Mockito.when(securityService.getUserId()).thenReturn("234567");
    AccessDO accessDO = this.createAccessDO();

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(accessDO)
        .when()
        .post("/")
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testDeleteAccess_Valid() {
    AccessDO accessDO = this.accessService.create(this.createAccessDO());

    given().when().delete("/" + accessDO.getId()).then().statusCode(204);
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testDeleteAccessAdmin_Valid() {
    Mockito.when(securityService.isAdmin()).thenReturn(true);
    AccessDO accessDO = this.accessService.create(this.createAccessDO());

    given().when().delete("/" + accessDO.getId()).then().statusCode(204);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testDeleteAccess_Invalid() {
    Mockito.when(securityService.getUserId()).thenReturn("234567");
    ContributorDO accessDO = this.accessService.getByDmpId(dmpDO.getId()).get(0);
    given().when().delete("/" + accessDO.getId()).then().statusCode(403);
  }

  private AccessDO createAccessDO() {
    AccessDO accessDO = new AccessDO();
    accessDO.setDmpId(dmpDO.getId());
    accessDO.setUniversityId(dmpDO.getContributors().get(0).getUniversityId());
    accessDO.setAccess(EFunctionRole.EDITOR);
    return accessDO;
  }
}
