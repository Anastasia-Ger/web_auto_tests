package iteration_2_middle.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CreateUserSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.Map;
import java.util.stream.Stream;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTests {
    private CreateUserRequest createUserRequest;
    private int userId;
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.8.39:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }
    @BeforeEach
    void setUp() {
        // Create user
        // Get createUserRequest and userId
        CreateUserSteps user = CreateUserSteps.createUser();
        createUserRequest = user.getRequest();
        userId = (int)user.getUserId();
    }
    @AfterEach
    // Clean up test data
    void deleteUsers() {
        AdminSteps.deleteUser(userId);
    }

    // Data for parameterized test:
    public static Stream<Arguments> dataForDepositWithValidAmount() {
        return Stream.of(
                Arguments.of(5000.0),
                Arguments.of(4999.0),
                Arguments.of(1.0)
        );
    }
    public static Stream<Arguments> dataForDepositWithInvalidAmount() {
        return Stream.of(
                Arguments.of(5001.0, "❌ Please deposit less or equal to 5000$."),
                Arguments.of(-1.0, "❌ Please enter a valid amount."),
                Arguments.of(0.0, "❌ Please enter a valid amount.")
        );
    }

    @MethodSource("dataForDepositWithValidAmount")
    @ParameterizedTest
    public void userCanDepositValidAmountOfMoneyTest(double amount) {

    // Preconditions
        // User creates an account
        String accountNumber = AdminSteps.createAccount(createUserRequest).getAccountNumber();
        // User logs in UI
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        open("/dashboard");

    // Steps
        //Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(accountNumber);

        // Enter valid amount
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("" + amount);

        // Click Deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

    // Expected result
    // Check that deposit is successful in UI
        // Validate and accept alert
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited");
        alert.accept();

        // Check redirect to User Dashboard
        $(Selectors.byText("User Dashboard")).shouldBe(Condition.visible);

        // Check that balance changed in UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText("Balance: $" + amount);

    // Check that deposit is successful in API
        // Get balance from account info
        double balanceAfterDeposit = UserSteps.getCustomerAccounts(createUserRequest.getUsername(),
                createUserRequest.getPassword()).getFirst().getBalance();
        assertThat(balanceAfterDeposit).isEqualTo(amount);
    }
    @MethodSource("dataForDepositWithInvalidAmount")
    @ParameterizedTest
    public void userCanNotDepositInvalidAmountOfMoneyTest(double amount, String actualAlertText) {
    // Preconditions
        // User creates an account
        int accountId = AdminSteps.createAccount(createUserRequest).getId();
        // User logs in UI
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        open("/dashboard");

    // Steps
        //Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText("" + accountId);

        // Enter valid amount
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys("" + amount);

        // Click Deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // Expected result
        // Check that deposit is not made
        // Validate and accept alert
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains(actualAlertText);
        alert.accept();

        // Check that balance does not change in UI
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText("Balance: $0.00");

    }
}
