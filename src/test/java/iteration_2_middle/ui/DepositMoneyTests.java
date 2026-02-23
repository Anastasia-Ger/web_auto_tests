package iteration_2_middle.ui;

import api.models.BankingTestData;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.CreateUserSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.WebDriverConditions;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositMoney;
import java.util.stream.Stream;
import static com.codeborne.selenide.Selenide.webdriver;
import static org.assertj.core.api.Assertions.assertThat;



public class DepositMoneyTests extends BaseUiTest {
    @AfterEach

    // Clean up test data
    void deleteUsers() {
        SessionStorage.clear();
    }

    // Data for parameterized test:
    public static Stream<Arguments> dataForDepositWithValidAmount() {
        return Stream.of(
                Arguments.of(BankingTestData.MAX_DEPOSIT),
                Arguments.of(BankingTestData.DEPOSIT_VALID_BELOW_MAX),
                Arguments.of(BankingTestData.DEPOSIT_VALID_MIN)
        );
    }
    public static Stream<Arguments> dataForDepositWithInvalidAmount() {
        return Stream.of(
                Arguments.of(BankingTestData.DEPOSIT_INVALID_ABOVE_MAX, BankAlert.PLEASE_ENTER_LESS_OR_EQUAL_TO_5000.getMessage()),
                Arguments.of(BankingTestData.DEPOSIT_INVALID_NEGATIVE, BankAlert.PLEASE_ENTER_VALID_AMOUNT.getMessage()),
                Arguments.of(BankingTestData.ZERO_DEPOSIT, BankAlert.PLEASE_ENTER_VALID_AMOUNT.getMessage())
        );
    }

    @MethodSource("dataForDepositWithValidAmount")
    @ParameterizedTest
    @UserSession()
    public void userCanDepositValidAmountOfMoneyTest(double amount) {

    // Preconditions
        // User creates an account
        String accountNumber = AdminSteps.createAccount(SessionStorage.getUser()).getAccountNumber();

    // Steps
        // Make deposit and check alert message
        new DepositMoney().open().makeDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage());

        // Check redirect to User Dashboard
        webdriver().shouldHave(WebDriverConditions.urlContaining("/dashboard"));

        // Check that balance changed in UI
        double actualBalance = new DepositMoney().open().getBalanceByAccountNumber(accountNumber);
        assertThat(actualBalance).isEqualTo(amount);

    // Check that deposit is successful in API
        // Get balance from account info
        double balanceAfterDeposit = UserSteps.getCustomerAccounts(SessionStorage.getUser().getUsername(),
                SessionStorage.getUser().getPassword()).getFirst().getBalance();
       // double balanceAfterDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
        //        createUserRequest.getPassword()).getFirst().getBalance();
        assertThat(balanceAfterDeposit).isEqualTo(amount);
    }
    @MethodSource("dataForDepositWithInvalidAmount")
    @ParameterizedTest
    @UserSession
    public void userCanNotDepositInvalidAmountOfMoneyTest(double amount, String actualAlertText) {
    // Preconditions
        // User creates an account
        String accountNumber = AdminSteps.createAccount(SessionStorage.getUser()).getAccountNumber();

    // Steps
        // Make deposit and check alert message
        new DepositMoney().open().makeDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(actualAlertText);

        // Check NO redirect to User Dashboard
        webdriver().shouldHave(WebDriverConditions.urlContaining("/deposit"));

        // Check that balance changed in UI
        double actualBalance = new DepositMoney().open().getBalanceByAccountNumber(accountNumber);
        assertThat(actualBalance).isZero();

    }
}
