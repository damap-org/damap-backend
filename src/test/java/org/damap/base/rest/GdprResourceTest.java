package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.damap.base.TestProfiles;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(GdprResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class GdprResourceTest {

  @InjectMock SecurityService securityService;

  @Inject TestDOFactory testDOFactory;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.getUserId()).thenReturn("012345");
    Mockito.when(securityService.getUserName()).thenReturn("testUser");
    testDOFactory.getOrCreateTestDmpDO();
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetGdprData_shouldReturnData() {
    given().when().get("/").then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetGdprExtendedData_shouldReturnData() {
    given().when().get("/extended").then().statusCode(200);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetGdprData_shouldReturnEmptyData() {
    Mockito.when(securityService.getUserId()).thenReturn("-1");

    String expectedResponse =
        "["
            + "{\"entity\":\"Consent\",\"entries\":[]},"
            + "{\"entity\":\"Access\",\"entries\":[]},"
            + "{\"entity\":\"Contributor\",\"entries\":[]}"
            + "]";

    String reponse = given().when().get("/").then().statusCode(200).extract().body().asString();

    assertEquals(expectedResponse, reponse);
  }
}
