package iteration_1.ui;

import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration_2_middle.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        // юзер теперь создается через экстеншен
   /*     CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);*/

        new UserDashboard().open().createNewAccount();

        // юзер-степс тоже получаем через экстеншен
        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() +
                createdAccounts.getFirst().getAccountNumber());

    }
}
