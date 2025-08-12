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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;
import java.util.List;

public class DepositMoneyTest extends BaseTest {

    // Data for parameterized test:
    public static Stream<Arguments> depositValidAmount() {
        return Stream.of(
                Arguments.of(5000.0),
                Arguments.of(4999.0),
                Arguments.of(1.0)
        );
    }
    public static Stream<Arguments> depositInvalidAmount() {
        return Stream.of(
                Arguments.of(5001.0, "Deposit amount exceeds the 5000 limit"),
                Arguments.of(-1.0, "Invalid account or amount")
        );
    }

@MethodSource("depositValidAmount")
@ParameterizedTest
public void userCanDepositMoneyWithValidAmount(double balance) {

    // Create a user
    CreateUserRequest userRequest = CreateUserRequest.builder()
            .username(RandomData.getUsername())
            .password(RandomData.getPassword())
            .role(UserRole.USER.toString())
            .build();


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


    // User deposits money
    DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
            .id(accountId)
            .balance(balance)
            .build();

    DepositMoneyResponse depositResponse = new DepositMoneyRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
            ResponseSpecs.requestReturnedOk())
            .post(depositRequest)
            .extract()
            .as(DepositMoneyResponse.class);

    softly.assertThat(depositRequest.getBalance()).isEqualTo(depositResponse.getBalance());

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

    @MethodSource("depositInvalidAmount")
    @ParameterizedTest
    public void userCanNotDepositMoneyWithInvalidAmount(double balance, String errorValue) {

        // Create a user
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();


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


        // User deposits money
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestInvalidAmount(errorValue))
                .post(depositRequest);


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
