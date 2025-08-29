package iteration_2_Junior;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class UpdateUsernameTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

    }
    @Test
    public void userCanUpdateUsernameWithValidDataTest() {

        // admin login
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                         {
                           "username":"admin",
                           "password":"admin"
                         }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // admin creates user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                         {
                           "username":"Max2222",
                           "password":"Max989898$",
                           "role":"USER"
                         }
                                                  """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("Max2222"))
                .body("password", Matchers.notNullValue());

        // user gets Auth token
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                         {
                           "username":"Max2222",
                           "password":"Max989898$"
                         }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        System.out.println("Extracted Auth token 1 is: " + userAuthToken);

        // user updates username with valid data
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                        {
                         "username":"Ben2222"
                        }
                        """

                )
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("username", Matchers.equalTo("Ben2222"));

        // get request to check that username changed
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("username", Matchers.equalTo("Ben2222"));

        /*
        Bug: update operation does not change username
        java.lang.AssertionError: 1 expectation failed.
JSON path username doesn't match.
Expected: Ben2222
  Actual: Max2222
         */

    }
    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(" ", "username", "Username can not be blank"),
                Arguments.of("bb", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("bb%", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("bb&", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void userCanNotUpdateUsernameWithInvalidDataTest(String username, String errorKey, String errorValue) {
        // user gets Auth token
        String userAuthToken2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                         {
                           "username":"Jlo56",
                           "password":"Max989898$"
                         }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");


        // create variable for username values in the body
        String requestBody = String.format(
                """
                        {
                         "username":"%s"
                        }
                        """, username
        );

        // user updates username with invalid data
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken2)
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header(errorKey, errorValue);

        // get request to check that username changed
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken2)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("username", Matchers.not(username));


    }
}
// Test result 1: Update of username doesn't update username
// Test result 2: There is no username validation on operation "Update username"
