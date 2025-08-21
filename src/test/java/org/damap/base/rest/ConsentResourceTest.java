package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.TestProfiles;
import org.damap.base.rest.administration.domain.ConsentDO;
import org.damap.base.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(ConsentResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class ConsentResourceTest {

  @InjectMock SecurityService securityService;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.getUserId()).thenReturn("012345");
    Mockito.when(securityService.getUserName()).thenReturn("testUser");
  }

  @Test
  void testGetConsentEndpoint_Invalid() {
    given().when().get().then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetConsentEndpoint_Valid() {
    given().when().get().then().statusCode(200).body("universityId", is("012345"));
  }

  @Test
  void testSaveConsentEndpoint_Invalid() {
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(mockConsentDO())
        .when()
        .post()
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testSaveConsentEndpoint_Valid() {
    given().when().get().then().statusCode(200);
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(mockConsentDO())
        .when()
        .post()
        .then()
        .statusCode(200)
        .body("universityId", is("012345"));
  }

  /**
   * mockConsentDO.
   *
   * @return a {@link org.damap.base.rest.administration.domain.ConsentDO} object
   */
  public ConsentDO mockConsentDO() {
    ConsentDO consentDO = new ConsentDO();
    consentDO.setUniversityId("012345");
    consentDO.setGivenDate(null);
    consentDO.setConsentGiven(true);
    return consentDO;
  }
}
