package iteration_2_middle.ui;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.CreateUserSteps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.BasePage;
import ui.pages.EditProfile;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;


public class UpdateNameTest extends BaseUiTest {
    private String userValidName;
    private CreateUserRequest createUserRequest;
    private int userId;

    @BeforeEach
    void setUp() {
        // Create user
        CreateUserSteps user = CreateUserSteps.createUser();
        createUserRequest = user.getRequest();
        userId = (int)user.getUserId();
        userValidName = RandomData.getName();
        // User logs in UI
        authAsUser(createUserRequest);
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
        String actualNewName = new EditProfile().open().updateName(userValidName)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .getUserProfileName();

        assertThat(actualNewName).isEqualTo(userValidName);

        BasePage.logout();

    // API checks
        String actualNewNameInAPI = AdminSteps.adminGetUsers().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("User not found: " + userId))
                .getName();

        assertThat(actualNewNameInAPI).isEqualTo(userValidName);
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
        String actualNewName = new EditProfile().open().updateName(name)
                .checkAlertMessageAndAccept(message)
                .getUserProfileName();
        assertThat(actualNewName).isEqualTo("Noname");

        BasePage.logout();

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
        new EditProfile().open().updateName(userValidName)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        // User updates name the second time with the same name
        String actualNewName = new EditProfile().open().updateName(userValidName)
                .checkAlertMessageAndAccept(BankAlert.NEW_NAME_IS_SAME_AS_CURRENT.getMessage())
                .getUserProfileName();
        assertThat(actualNewName).isEqualTo(userValidName);

        BasePage.logout();

    }
}
