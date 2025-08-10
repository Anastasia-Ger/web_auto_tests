package iteration_2_middle;

import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import iteration_1.BaseTest;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.DepositMoneyResponse;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class DepositMoneyTest extends BaseTest {
@Test
public void userCanDepositMoney() {

    CreateUserRequest userRequest = CreateUserRequest.builder()
            .username(RandomData.getUsername())
            .password(RandomData.getPassword())
            .role(UserRole.USER.toString())
            .build();


    // Create a user
    int userId = new AdminCreateUserRequester(
            RequestSpecs.adminSpec(),
            ResponseSpecs.entityWasCreated())
            .post(userRequest)
            .extract()
            .path("id");


    // User creates an account
    int accountId = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
            ResponseSpecs.entityWasCreated())
            .post(null)
            .extract()
            .path("id");


    // User deposits money - maximum allowed limit (5000)
    DepositMoneyRequest depositRequestMaxLimit = DepositMoneyRequest.builder()
            .id(accountId)
            .balance(5000)
            .build();

    DepositMoneyResponse depositResponse =  new DepositMoneyRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
            ResponseSpecs.requestReturnedOk())
            .post(depositRequestMaxLimit)
            .extract()
            .as(DepositMoneyResponse.class);
    softly.assertThat(depositRequestMaxLimit.getBalance()).isEqualTo(depositResponse.getBalance());


    // User deposits money over limit 5000, upper boundary value: 5001
    // Negative test
    DepositMoneyRequest depositRequestOverLimit = DepositMoneyRequest.builder()
            .id(accountId)
            .balance(5001)
            .build();

    new DepositMoneyRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
            ResponseSpecs.requestReturnsBadRequestDepositOverLimit())
            .post(depositRequestOverLimit);


    // User deposits money within the limit, lower boundary value: 4999
    DepositMoneyRequest depositRequestWithinLimit = DepositMoneyRequest.builder()
            .id(accountId)
            .balance(4999)
            .build();

    new DepositMoneyRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
            ResponseSpecs.requestReturnedOk())
            .post(depositRequestWithinLimit);


    // Get customer accounts
    new GetAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(),
            userRequest.getPassword()),
            ResponseSpecs.requestReturnedOk())
            .get(null);


    // Delete user
    ValidatableResponse responseSpecification = new DeleteUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnedOk())
            .delete(userId);
    softly.assertThat(responseSpecification.body(Matchers.equalTo("User with ID " + userId + " deleted successfully.")));


    softly.assertAll();

    }
}
