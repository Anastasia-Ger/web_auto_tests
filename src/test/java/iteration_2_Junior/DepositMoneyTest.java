package iteration_2_Junior;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class DepositMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

    }

    @Test
    public void userCanDepositMoneyTest() {

        // Admin login
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

        // Admin creates user
        int userId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                         {
                           "username":"Tom66",
                           "password":"Max989898$",
                           "role":"USER"
                         }
                                                  """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("Tom66"))
                .body("password", Matchers.notNullValue())
                .extract()
                .path("id");

        // User gets Auth token
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                         {
                           "username":"Tom66",
                           "password":"Max989898$"
                         }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // User creates account
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // User deposits money with invalid amount (more than 5000 per transaction)
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                        {
                         "id":19,
                         "balance":5001
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                        .body(Matchers.equalTo("Deposit amount exceeds the 5000 limit"));



        // User deposits money with valid amount (5000 per transaction)
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                        {
                         "id":19,
                         "balance":5000
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        // User deposits money with valid amount (lower border value 4999)
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body("""
                        {
                         "id":19,
                         "balance":4999
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        // Get customer accounts
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Delete user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .delete("http://localhost:4111/api/v1/admin/users/" + userId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

    }
}
