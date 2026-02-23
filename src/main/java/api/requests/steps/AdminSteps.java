package api.requests.steps;

import api.generators.RandomModelGenerator;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.GetUsersResponse;
import org.hamcrest.Matchers;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
    // Возможно это метод по получению всех юзеров, который я создала
    public static List<GetUsersResponse> adminGetUsers() {

        var users = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_GET_USERS,
                ResponseSpecs.requestReturnedOk())
                .get(new TypeRef<List<GetUsersResponse>>() {});

        return users;
    }
    // А это с Сашей на UI-middle создали
    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnedOk()
        ).getAll(CreateUserResponse[].class);
    }
}
