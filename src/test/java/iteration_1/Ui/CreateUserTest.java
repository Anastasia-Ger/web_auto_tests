package iteration_1.Ui;

import com.codeborne.selenide.*;
import comparison.ModelAssertions;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import specs.RequestSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
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
    public void AdminCanCreateUserTest() {
        // Step1: admin login
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // Step2: admin creates user
        // generate data for a new user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        // go to UI and create user
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        // Step3: check alert that user created successfully
        Alert alert = switchTo().alert();
        assertEquals("✅ User created successfully!", alert.getText());
        alert.accept();

        // Step4: check that user is visible in UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users"))
                .parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldBe(Condition.visible);

        // Step5: check that user is created in API
        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        CreateUserResponse createdUser = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();

    }
    @Test
    public void AdminCanNotCreateUserWithInvalidDataTest()  {
        // Step1: admin login
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();
        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // Step2: admin creates user
        // generate data for a new user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        // go to UI and create user with invalid username
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();


        // Step3: check alert that user can not be created
        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains("Username must be between 3 and 15 characters"));
        alert.accept();

        // Step4: check that user is NOT visible in UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users"))
                .parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldNot(Condition.exist);

        // Step5: check that user was NOT created in API
        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        long usersWithSameUsernameAsUser = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();
        assertThat(usersWithSameUsernameAsUser).isZero();


    }
}
