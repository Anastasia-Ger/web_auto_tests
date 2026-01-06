package iteration_1.Ui;

import api.comparison.ModelAssertions;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Condition;
import iteration_2_middle.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserTest extends BaseUiTest {
    @Test
    public void AdminCanCreateUserTest() {
        // Step1: admin login
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin.getUsername(), admin.getPassword());

        // Step2: admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        // Step3: check alert that user created successfully
        // Step 4: check that user is visible in UI
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage()).getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);


        // Step5: check that user is created in API
        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();

    }
    @Test
    public void AdminCanNotCreateUserWithInvalidDataTest()  {
        // Step1: admin login
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        // Step2: admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        // go to UI and create user with invalid username
        // Step3: check alert that user can not be created
        // Step4: check that user is NOT visible in UI
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().findBy(Condition.exactText((newUser.getUsername() + "\nUSER")))
                .shouldNotBe(Condition.exist);

        // Step5: check that user was NOT created in API
        long usersWithSameUsernameAsUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();
        assertThat(usersWithSameUsernameAsUser).isZero();


    }
}
