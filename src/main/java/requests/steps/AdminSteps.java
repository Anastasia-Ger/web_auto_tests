package requests.steps;

import generators.RandomModelGenerator;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.GetUsersResponse;
import org.hamcrest.Matchers;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);
        return userRequest;
    }

    public static CreateAccountResponse createAccount(CreateUserRequest userRequest) {

        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }

    public static ValidatableResponse deleteUser(int userId) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.DELETE, ResponseSpecs.requestReturnedOk())
                .delete(userId);
    }
    public static List<GetUsersResponse> adminGetUsers() {

        var users = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_GET_USERS,
                ResponseSpecs.requestReturnedOk())
                .get(new TypeRef<List<GetUsersResponse>>() {});

        return users;
    }
}
