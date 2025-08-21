package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;

import org.damap.base.TestProfiles;
import org.damap.base.rest.storage.InternalStorageDO;
import org.damap.base.rest.storage.InternalStorageTranslationDO;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(InternalStorageResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class InternalStorageResourceTest {

  @Inject TestDOFactory testDOFactory;

  @BeforeAll
  public static void setup() {
    RestAssured.defaultParser = Parser.JSON;
  }

  private InternalStorageDO getValidInternalStorageDO() {
    InternalStorageDO validData = new InternalStorageDO();
    validData.setUrl("test.url.com");
    validData.setStorageLocation("testStorageLocation");
    validData.setBackupLocation("testBackupLocation");
    validData.setActive(true);

    InternalStorageTranslationDO translation = new InternalStorageTranslationDO();
    translation.setLanguageCode("eng");
    translation.setTitle("testStorageName");
    translation.setDescription("testStorageDescription");
    translation.setBackupFrequency("testBackupFrequency");

    validData.setTranslations(List.of(translation));

    return validData;
  }

  @Test
  void testGetAllByLanguageEndpoint_Invalid() {
    given().when().get("languageCode=eng").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetAllByLanguageEndpoint_Valid() {
    testDOFactory.prepareInternalStorageOption();
    given().queryParam("languageCode", "eng").when().get().then().statusCode(200);
  }

  // Authorization tests
  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testPostEndpointAsUser_Unauthorized() {
    given().contentType("application/json").when().post().then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testPutEndpointAsUser_Unauthorized() {
    given().contentType("application/json").when().put("/1").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testDeleteEndpointAsUser_Unauthorized() {
    given().when().delete("/1").then().statusCode(403);
  }

  // GET tests
  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testReadEndpointAsUser_NotFound() {
    given().when().get("/-1").then().statusCode(404);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testReadEndpointAsUser_BadRequest() {
    ValidatableResponse response = given().when().get("/abc").then().statusCode(400);
    response.body("message", startsWith("Invalid internal storage ID"));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testReadEndpointAsUser_Valid() {
    Long id = testDOFactory.prepareInternalStorageOption();

    InternalStorageDO response =
        given().when().get("/" + id).then().statusCode(200).extract().as(InternalStorageDO.class);

    assertEquals(id, response.getId());
    assertEquals("test.url.com", response.getUrl());
  }

  // POST tests
  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testCreateEndpointAsAdminWithMissingTranslation_BadRequest() {
    InternalStorageDO invalidData = this.getValidInternalStorageDO();
    invalidData.setTranslations(List.of());

    ValidatableResponse response =
        given()
            .contentType("application/json")
            .body(invalidData)
            .when()
            .post()
            .then()
            .statusCode(400);
    response.body("message", startsWith("Translations list cannot be null or empty"));
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testCreateEndpointAsAdminWithEmptyURL_BadRequest() {
    InternalStorageDO invalidData = this.getValidInternalStorageDO();
    invalidData.setUrl("");

    ValidatableResponse response =
        given()
            .contentType("application/json")
            .body(invalidData)
            .when()
            .post()
            .then()
            .statusCode(400);

    response.body("exception", equalTo("ConstraintViolationException"));
    response.body("violations.size()", is(1));
    response.body("violations[0].message", startsWith("url cannot be blank"));
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testCreateEndpointAsAdminWithValidData_Created() {
    InternalStorageDO validData = this.getValidInternalStorageDO();

    InternalStorageDO response =
        given()
            .contentType("application/json")
            .body(validData)
            .when()
            .post()
            .then()
            .statusCode(200)
            .extract()
            .as(InternalStorageDO.class);

    Long id = response.getId();
    assertEquals("test.url.com", response.getUrl());

    InternalStorageDO readResponse =
        given().when().get("/" + id).then().statusCode(200).extract().as(InternalStorageDO.class);
    assertEquals(id, readResponse.getId());
    assertEquals("test.url.com", readResponse.getUrl());
    assertEquals("testStorageLocation", readResponse.getStorageLocation());
    assertEquals("testBackupLocation", readResponse.getBackupLocation());
    assertEquals(true, readResponse.getActive());
  }

  // PUT tests
  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testPutEndpointAsAdminWithNonExistingId_NotFound() {
    InternalStorageDO validData = this.getValidInternalStorageDO();
    given()
        .contentType("application/json")
        .body(validData)
        .when()
        .put("/-1")
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testPutEndpointAsAdminValid_Updated() {
    Long id = testDOFactory.prepareInternalStorageOption();

    InternalStorageDO currentData =
        given().when().get("/" + id).then().statusCode(200).extract().as(InternalStorageDO.class);
    String currentUrl = currentData.getUrl();

    InternalStorageDO validData = this.getValidInternalStorageDO();
    validData.setUrl("new.url.com");

    InternalStorageDO response =
        given()
            .contentType("application/json")
            .body(validData)
            .when()
            .put("/" + id)
            .then()
            .statusCode(200)
            .extract()
            .as(InternalStorageDO.class);

    assertEquals(id, response.getId());
    assertEquals("new.url.com", response.getUrl());
    assertNotEquals(currentUrl, response.getUrl());
  }

  // DELETE tests
  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testDeleteEndpointAsAdminWithNonExistingId_NotFound() {
    given().when().delete("/-1").then().statusCode(404);
  }

  @Test
  @TestSecurity(user = "adminJwt", roles = "Damap Admin")
  void testDeleteEndpointAsAdminValidId_Deleted() {
    Long id = testDOFactory.prepareInternalStorageOption();
    given().when().delete("/" + id).then().statusCode(204);

    given().when().get("/" + id).then().statusCode(404);
  }

  // Search tests
  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSearchEndpointAsUserInvalidParams_IgnoredAndFiltered() {
    testDOFactory.prepareSpecialInternalStorageOption("1");

    given()
        .queryParams("invalidParam", "1", "url", "special.url.com1")
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", equalTo(1));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSearchEndpointAsUserMultipleOptions_Filtered() {
    testDOFactory.prepareSpecialInternalStorageOption("2");
    testDOFactory.prepareSpecialInternalStorageOption("3");

    MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
    queryParams.put("url", List.of("special.url.com2", "special.url.com3"));
    queryParams.put("active", List.of("true"));

    given()
        .queryParams(queryParams)
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", equalTo(2));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSearchEndpointAsUserMultipleOptions_FilteredNoResult() {
    testDOFactory.prepareSpecialInternalStorageOption("4");
    testDOFactory.prepareSpecialInternalStorageOption("5");

    MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
    queryParams.put("url", List.of("special.url.com4", "special.url.com5"));
    queryParams.put("active", List.of("false"));

    given()
        .queryParams(queryParams)
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", equalTo(0));
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSearchEndpointAsUserValidParams_Filtered() {
    testDOFactory.prepareSpecialInternalStorageOption("");

    given()
        .queryParam("url", "special.url.com")
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", equalTo(1));
  }
}
