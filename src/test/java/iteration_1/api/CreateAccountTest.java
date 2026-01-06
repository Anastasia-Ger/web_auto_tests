package iteration_1.api;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.UserRole;
import org.junit.jupiter.api.Test;
import api.requests.AdminCreateUserRequester;
import api.requests.CreateAccountRequester;
import api.requests.LoginUserRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class CreateAccountTest {
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();



        // Создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);


        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

    }

}
