package iteration_1.api;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.AdminCreateUserRequester;
import api.requests.LoginUserRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {
    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();
        new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnedOk())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);


        new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnedOk())
                .post(LoginUserRequest.builder()
                        .username(createUserRequest.getUsername())
                        .password(createUserRequest.getPassword())
                        .build())
                .header("Authorization", Matchers.notNullValue());
    }
}
