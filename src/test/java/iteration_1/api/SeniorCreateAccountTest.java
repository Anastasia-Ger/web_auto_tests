package iteration_1.api;

import io.restassured.common.mapper.TypeRef;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.GetAccountResponse;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class SeniorCreateAccountTest extends BaseTest {
    @Test
    public void userCanCreateAccountTest() {
        // Создание пользователя
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создание аккаунта пользователем
        CreateAccountResponse response = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // Get user accounts
        var accounts = new ValidatedCrudRequester<>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnedOk()
        ).get(new TypeRef<List<GetAccountResponse>>() {});

        softly.assertThat(response.getAccountNumber()).isEqualTo(accounts.getFirst().getAccountNumber());
        softly.assertAll();
    }
}
