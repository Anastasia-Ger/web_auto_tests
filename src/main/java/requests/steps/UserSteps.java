package requests.steps;

import io.restassured.common.mapper.TypeRef;
import models.GetAccountResponse;
import models.GetUsersResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class UserSteps {

    public static List<GetAccountResponse> getCustomerAccounts(String username, String password) {

        var accounts = new ValidatedCrudRequester<>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnedOk())
                .get(new TypeRef<List<GetAccountResponse>>() {});

        return accounts;
    }

}
