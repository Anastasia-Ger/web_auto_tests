package iteration_2_middle.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CreateUserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.Map;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTests {
    private CreateUserRequest createSenderRequest;
    private CreateUserRequest createRecipientRequest;
    private int senderId;
    private int recipientId;
    private int senderAccountId;
    private String senderAccountNumber;
    private int recipientAccountId;
    private String recipientAccountNumber;
    private String recipientName;
    private String invalidRecipientAccountNumber;
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
    // Preconditions:
        // Create sender and his account
        CreateUserSteps sender = CreateUserSteps.createUser();
        createSenderRequest = sender.getRequest();
        senderId = (int)sender.getUserId();
        CreateAccountResponse senderResponse = AdminSteps.createAccount(createSenderRequest);
        senderAccountId = senderResponse.getId();
        senderAccountNumber = senderResponse.getAccountNumber();

        // Accumulate balance for transfer check
        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(BankingTestData.MAX_DEPOSIT)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(createSenderRequest.getUsername(),
                createSenderRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        // Create recipient and his account
        CreateUserSteps recipient = CreateUserSteps.createUser();
        createRecipientRequest = recipient.getRequest();
        recipientId = (int)recipient.getUserId();
        CreateAccountResponse recipientResponse = AdminSteps.createAccount(createRecipientRequest);
        recipientAccountId = recipientResponse.getId();
        recipientAccountNumber = recipientResponse.getAccountNumber();

        // Create recipient name
        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name(RandomData.getName())
                .build();


        recipientName = new ValidatedCrudRequester<UpdateNameResponse>
                (RequestSpecs.authAsUser(createRecipientRequest.getUsername(),
                        createRecipientRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_NAME,
                        ResponseSpecs.requestReturnedOk())
                .update(updateNameRequest).getCustomer().getName();

        // Sender logs in UI
        String senderAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createSenderRequest.getUsername()).password(createSenderRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", senderAuthHeader);
        open("/dashboard");

    }
    @AfterEach
        // Clean up test data
    void deleteUsers() {
        AdminSteps.deleteUser(senderId);
        AdminSteps.deleteUser(recipientId);
    }
    @Test
    public  void userCanMakeTransferWithValidDataTest() {
    // Steps:
        // Sender makes a transfer
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(recipientName);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys("" + BankingTestData.TRANSFER_VALID_AMOUNT);
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

    // UI checks
        // Check that alert appears and has correct message
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred");
        alert.accept();

        Selenide.refresh();

        // Check that balance changes in sender account
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);

        String senderAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();
        String senderCurrentBalance = senderAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double senderActualBalance = Double.parseDouble(senderCurrentBalance);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT  - BankingTestData.TRANSFER_VALID_AMOUNT);

        // Check that balance changes in recipient account
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // Recipient logs in
        String recipientAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createRecipientRequest.getUsername()).password(createRecipientRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", recipientAuthHeader);
        open("/dashboard");

        // Get balance via Deposit Money btn
        // Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(recipientAccountNumber);

        // Get recipient balance value
        String recipientAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();

        String recipientBalance = recipientAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double recipientActualBalance = Double.parseDouble(recipientBalance);
        assertThat(recipientActualBalance).isEqualTo(BankingTestData.TRANSFER_VALID_AMOUNT);

        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

    // API checks
        // Check that sender balance changes
        double actualSenderBalance = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == senderId)
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Account not found: " + senderAccountNumber))
                .getBalance();

        assertThat(actualSenderBalance).isEqualTo(BankingTestData.MAX_DEPOSIT  - BankingTestData.TRANSFER_VALID_AMOUNT);

        // Check that recipient balance changes
        double actualRecipientBalance = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == recipientId)
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountNumber().equals(recipientAccountNumber))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Account not found: " + recipientAccountNumber))
                .getBalance();

        assertThat(actualRecipientBalance).isEqualTo(BankingTestData.TRANSFER_VALID_AMOUNT);

    }
    @Test
    public void userCanNotTransferToInvalidAccount() {
        invalidRecipientAccountNumber = "ACC85";
    // Steps:
        // Sender makes a transfer
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(recipientName);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(invalidRecipientAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys("" + BankingTestData.TRANSFER_VALID_AMOUNT);
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // UI checks
        // Check that alert appears and has correct message
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo("❌ No user found with this account number.");
        alert.accept();

        Selenide.refresh();

        // Check that balance does not change in sender account
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);

        String senderAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();
        String senderCurrentBalance = senderAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double senderActualBalance = Double.parseDouble(senderCurrentBalance);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that balance does not change in recipient account
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // Recipient logs in
        String recipientAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createRecipientRequest.getUsername()).password(createRecipientRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", recipientAuthHeader);
        open("/dashboard");

        // Get balance via Deposit Money btn
        // Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(recipientAccountNumber);

        // Get recipient balance value
        String recipientAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();

        String recipientBalance = recipientAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double recipientActualBalance = Double.parseDouble(recipientBalance);
        assertThat(recipientActualBalance).isZero();

        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

    }
    @Test
    public void userCanNotTransferMoreThanMaximumAllowedAmount() {
    // Steps:
        // Sender makes a transfer
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(recipientName);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys("" + BankingTestData.TRANSFER_INVALID_UPPER);
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

    // UI checks
        // Check that alert appears and has correct message
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo("❌ Error: Transfer amount cannot exceed 10000");
        alert.accept();

        Selenide.refresh();

        // Check that balance does not change in sender account
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);

        String senderAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();
        String senderCurrentBalance = senderAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double senderActualBalance = Double.parseDouble(senderCurrentBalance);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that balance does not change in recipient account
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // Recipient logs in
        String recipientAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createRecipientRequest.getUsername()).password(createRecipientRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", recipientAuthHeader);
        open("/dashboard");

        // Get balance via Deposit Money btn
        // Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(recipientAccountNumber);

        // Get recipient balance value
        String recipientAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();

        String recipientBalance = recipientAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double recipientActualBalance = Double.parseDouble(recipientBalance);
        assertThat(recipientActualBalance).isZero();

        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

    // API checks
        // Check that sender balance does not change
        double actualSenderBalance = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == senderId)
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Account not found: " + senderAccountNumber))
                .getBalance();

        assertThat(actualSenderBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that recipient balance does not change
        double actualRecipientBalance = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == recipientId)
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountNumber().equals(recipientAccountNumber))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Account not found: " + recipientAccountNumber))
                .getBalance();

        assertThat(actualRecipientBalance).isZero();
    }
    @Test
    public void userCanNotTransferWithEmptyRecipientName() {
    // Steps:
        // Sender makes a transfer
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(recipientAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .sendKeys("" + BankingTestData.TRANSFER_INVALID_UPPER);
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // UI checks
        // Check that alert appears and has correct message
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo("❌ The recipient name does not match the registered name.");
        alert.accept();

        Selenide.refresh();

        // Check that balance does not change in sender account
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);

        String senderAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();
        String senderCurrentBalance = senderAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double senderActualBalance = Double.parseDouble(senderCurrentBalance);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that balance does not change in recipient account
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // Recipient logs in
        String recipientAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createRecipientRequest.getUsername()).password(createRecipientRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", recipientAuthHeader);
        open("/dashboard");

        // Get balance via Deposit Money btn
        // Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(recipientAccountNumber);

        // Get recipient balance value
        String recipientAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();

        String recipientBalance = recipientAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double recipientActualBalance = Double.parseDouble(recipientBalance);
        assertThat(recipientActualBalance).isZero();

        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

    }
    @Test
    public void userCanNotTransferWithMissingRequiredFields() {
    // Steps:
        // Sender makes a transfer
        // Amount is missing
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .sendKeys(recipientName);
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(recipientAccountNumber);
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

    // UI checks
        // Check that alert appears and has correct message
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo("❌ Please fill all fields and confirm.");
        alert.accept();

        Selenide.refresh();

        // Check that balance does not change in sender account
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector"))
                .selectOptionContainingText(senderAccountNumber);

        String senderAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();
        String senderCurrentBalance = senderAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double senderActualBalance = Double.parseDouble(senderCurrentBalance);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that balance does not change in recipient account
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // Recipient logs in
        String recipientAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(createRecipientRequest.getUsername()).password(createRecipientRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", recipientAuthHeader);
        open("/dashboard");

        // Get balance via Deposit Money btn
        // Choose an account
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byClassName("account-selector")).click();
        $(Selectors.byCssSelector("select.account-selector")).selectOptionContainingText(recipientAccountNumber);

        // Get recipient balance value
        String recipientAccountText = $(Selectors.byCssSelector("select.account-selector"))
                .getSelectedOption()
                .getText();

        String recipientBalance = recipientAccountText.replaceAll(".*Balance:\\s*\\$\\s*([0-9]+\\.[0-9]{2}).*",
                "$1");
        double recipientActualBalance = Double.parseDouble(recipientBalance);
        assertThat(recipientActualBalance).isZero();

        $(Selectors.byText("\uD83D\uDEAA Logout")).click();
    }
}
