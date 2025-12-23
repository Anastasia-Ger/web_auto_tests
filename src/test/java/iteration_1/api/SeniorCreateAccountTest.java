package iteration_1.api;

import io.restassured.common.mapper.TypeRef;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.GetAccountResponse;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
