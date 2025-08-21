package org.damap.base.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestHTTPEndpoint(MaDmpResource.class)
@TestProfile(TestProfiles.DefaultProfile.class)
class MaDmpResourceTest extends TestSetup {

  @Test
  void testGetByIdEndpoint_Invalid() {
    given().when().get("/0").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetByIdEndpoint_Unauthorized() {
    given().when().get("/0").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetByIdEndpoint_Valid() {
    given().when().get("/" + dmpDO.getId()).then().statusCode(200);
  }

  @Test
  void testGetFileByIdEndpoint_Invalid() {
    given().when().get("/file/0").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetFileByIdEndpoint_Unauthorized() {
    given().when().get("/file/0").then().statusCode(403);
  }

  @Test
  @TestSecurity(user = "userJwt", roles = "user")
  void testGetFileByIdEndpoint_Valid() {
    given().when().get("/file/" + dmpDO.getId()).then().statusCode(200);
  }
}
