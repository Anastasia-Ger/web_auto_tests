package iteration_1.Ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
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
    @Test
    public void userCanCreateAccountTest() {
    // Environment setup steps
        // Step 1: admin login
        // Step 2: admin creates user
        // Step 3: user login
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(),
                 Endpoint.LOGIN, ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        System.out.println("Auth Token: " + userAuthHeader);

        Selenide.open("/");

        // Put authentication token into Local Storage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

    // Test steps
        // Step 4: user creates account
        $(Selectors.byText("➕ Create New Account")).click();

        // Step 5: check that account created in UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ New Account Created! Account Number:");
        alert.accept();

        // Get account number from alert text
        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);
        matcher.find();
        String createdAccountNumber = matcher.group(1);

        // Step 6: check that account is created in API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse createdAccount = Arrays.stream(existingUserAccounts)
                .filter(account -> account.getAccountNumber().equals(createdAccountNumber))
                .findFirst().get();
        assertThat(createdAccount.getBalance()).isZero();
        assertThat(existingUserAccounts).hasSize(1);


    }
}
