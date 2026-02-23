package api.requests.steps;

import api.models.AccountResponse;
import api.models.CreateAccountResponse;
import io.restassured.common.mapper.TypeRef;
import api.models.GetAccountResponse;
import api.models.GetUsersResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // Сашин метод по получению акк.юзера с Middle UI
    public  List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnedOk()).getAll(CreateAccountResponse[].class);
    }


// Это мой метод по получению аккаунтов юзера
    public static List<GetAccountResponse> getCustomerAccounts(String username, String password) {

        var accounts = new ValidatedCrudRequester<>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnedOk())
                .get(new TypeRef<List<GetAccountResponse>>() {});

        return accounts;
    }
    public double getBalance(String accountNumber) {

        return getAllAccounts().stream()
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Account not found: " + accountNumber))
                .getBalance();
    }

}
