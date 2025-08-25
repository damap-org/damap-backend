package org.damap.base.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(DmpDocumentResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class DmpDocumentResourceTest extends TestSetup {

  @Test
  void testExportTemplateEndpoint_Invalid() {
    given().when().get("/0").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportTemplateEndpoint_Unauthorized() {
    given().when().get("/0").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportTemplateEndpoint_Valid() {
    given().when().get("/" + dmpDO.getId()).then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportTemplateEndpointWithTemplateTypeFWF_Valid() {
    given().when().get("/" + dmpDO.getId() + "?template=FWF").then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportTemplateEndpointWithTemplateTypeScienceEurope_Valid() {
    given().when().get("/" + dmpDO.getId() + "?template=SCIENCE_EUROPE").then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportTemplateEndpointWithTemplateType_Invalid() {
    given().when().get("/" + dmpDO.getId() + "?template=invalid").then().statusCode(404);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportEndpointForPDFWithTemplateTypeScienceEurope_Valid() {
    given()
        .when()
        .get("/" + dmpDO.getId() + "/export?template=SCIENCE_EUROPE&fileType=docx")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportEndpointForPDFWithTemplateTypeHorizonEurope_Valid() {
    given()
        .when()
        .get("/" + dmpDO.getId() + "/export?template=HORIZON_EUROPE&fileType=docx")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testExportEndpointForPDFWithTemplateTypeFWF_Valid() {
    given()
        .when()
        .get("/" + dmpDO.getId() + "/export?template=FWF&fileType=docx")
        .then()
        .statusCode(200);
  }
}
