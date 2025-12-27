package iteration_2_middle.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CreateUserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import java.util.Map;
import java.util.stream.Stream;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateNameTests {
    private String userValidName;
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
    }
    @AfterEach
        // Clean up test data
    void deleteUsers() {
        AdminSteps.deleteUser(userId);
    }

    @Test
    public void userCanUpdateNameWithValidData() {
    // Steps
        // User updates name
        userValidName = RandomData.getName();
        Selenide.refresh();
        $(Selectors.byClassName("profile-header")).should(Condition.exist).shouldBe(Condition.visible)
                .shouldBe(Condition.enabled).click();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible).shouldBe(Condition.enabled).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.empty).shouldBe(Condition.exist).sendKeys(userValidName);

        $(Selectors.byText("\uD83D\uDCBE Save Changes"))
                .shouldBe(Condition.enabled).click();


    // UI checks
        Alert alert1 = switchTo().alert();
        String alertText1 = alert1.getText();
        assertThat(alertText1).isEqualTo("✅ Name updated successfully!");
        alert1.accept();

        Selenide.refresh();

        String actualProfileName = $("span.user-name").getText();
        assertThat(actualProfileName).isEqualTo(userValidName);
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

    // API checks
        String actualName = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("User not found: " + userId))
                .getName();

        assertThat(actualName).isEqualTo(userValidName);
    }
    // Data for parameterized test
    public static Stream<Arguments> dataForUpdateNameWithInvalidData() {
        return Stream.of(
                Arguments.of("Helen", "❌ Please enter a valid name."),
                Arguments.of("", "❌ Please enter a valid name."),
                Arguments.of(" ", "❌ Please enter a valid name.")
        );
    }
    @MethodSource("dataForUpdateNameWithInvalidData")
    @ParameterizedTest
    public void userCanNotUpdateNameWithInvalidData(String name, String message) {
    // Steps
        // User updates name
        $(Selectors.byClassName("profile-header")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible).shouldBe(Condition.enabled).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(name);
        $(Selectors.byText("\uD83D\uDCBE Save Changes"))
                .shouldBe(Condition.enabled).click();

    // UI checks
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo(message);
        alert.accept();

        Selenide.refresh();

        String actualProfileName = $("span.user-name").getText();
        assertThat(actualProfileName).isEqualTo("Noname");
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // API checks
        String actualName = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("User not found: " + userId))
                .getName();

        assertThat(actualName).isNull();
    }
    @Test
    public void userCanNotUpdateNameWithCurrentName() {
    // Steps
        // User updates name the first time
        userValidName = RandomData.getName();
        Selenide.refresh();
        $(Selectors.byClassName("profile-header")).shouldBe(Condition.exist).shouldBe(Condition.visible)
                .shouldBe(Condition.enabled).click();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible).shouldBe(Condition.enabled).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.enabled).sendKeys(userValidName);
        $(Selectors.byText("\uD83D\uDCBE Save Changes"))
                .shouldBe(Condition.enabled).click();

        // User updates name the second time with the same name
        Selenide.refresh();
        $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(Condition.exist)
                .shouldBe(Condition.enabled).click();
        $(Selectors.byClassName("profile-header")).shouldBe(Condition.exist).shouldBe(Condition.visible)
                .shouldBe(Condition.enabled).click();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible).shouldBe(Condition.enabled).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.enabled).sendKeys(userValidName);
        $(Selectors.byText("\uD83D\uDCBE Save Changes"))
                .shouldBe(Condition.enabled).click();

    // UI checks
        Alert alert2 = switchTo().alert();
        String alertText2 = alert2.getText();
        assertThat(alertText2).isEqualTo("⚠\uFE0F New name is the same as the current one.");
        System.out.println("Alert assert OK!");
        alert2.accept();

        Selenide.refresh();

        String actualProfileName = $("span.user-name").getText();
        assertThat(actualProfileName).isEqualTo(userValidName);
        System.out.println("UI assert OK!");
        System.out.println("актуальное имя " + actualProfileName);
        System.out.println("валидное имя " + userValidName);
        $(Selectors.byText("\uD83D\uDEAA Logout")).click();

        // API checks
        String actualName = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("User not found: " + userId))
                .getName();

        assertThat(actualName).isEqualTo(userValidName);
        System.out.println("API assert OK!");
    }
}
