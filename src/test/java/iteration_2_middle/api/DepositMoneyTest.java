package iteration_2_middle.api;

import iteration_1.api.BaseTest;
import api.models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.CreateUserSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    private CreateUserRequest createUserRequest;
    private int userId;
    @BeforeEach
    void setUp() {
        // Create user
        // Get an object that stores CreateUserRequest and userId
        CreateUserSteps user = CreateUserSteps.createUser();
        createUserRequest = user.getRequest();
        userId = (int)user.getUserId();
    }
    @AfterEach
    void deleteUsers() {
        AdminSteps.deleteUser(userId);
    }

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

    // Create an account and get account ID
    int accountId = AdminSteps.createAccount(createUserRequest).getId();

    // Get balance from account info before deposit
    double balanceBeforeDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
            createUserRequest.getPassword()).getFirst().getBalance();

    // Deposit money
    DepositRequest depositRequest = DepositRequest.builder()
            .id(accountId)
            .balance(balance)
            .build();

    DepositResponse response = new ValidatedCrudRequester<DepositResponse>(RequestSpecs.authAsUser(createUserRequest.getUsername(),
            createUserRequest.getPassword()),
            Endpoint.DEPOSIT, ResponseSpecs.requestReturnedOk())
            .post(depositRequest);

    softly.assertThat(depositRequest.getBalance()).isEqualTo(response.getBalance());

    // Get balance from account info after deposit
    double balanceAfterDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
            createUserRequest.getPassword()).getFirst().getBalance();

    // Check that balance changed by the amount of deposit
    softly.assertThat(balanceAfterDeposit).isEqualTo(balanceBeforeDeposit + depositRequest.getBalance());

    softly.assertAll();

}
@Disabled
    @MethodSource("depositInvalidAmount")
    @ParameterizedTest
    public void userCanNotDepositMoneyWithInvalidAmount(double balance, String errorValue) {

        // Create an account
        int accountId = AdminSteps.createAccount(createUserRequest).getId();

        // Get balance from account info before deposit
        double balanceBeforeDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
                createUserRequest.getPassword()).getFirst().getBalance();

        // Deposit money
        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestInvalidAmount(errorValue))
                .post(depositRequest);


        // Get balance from account info after deposit
        double balanceAfterDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
                createUserRequest.getPassword()).get(0).getBalance();

        // Check that balance have not changed by the amount of deposit
        softly.assertThat(balanceAfterDeposit).isEqualTo(balanceBeforeDeposit);

        softly.assertAll();
    }
}
