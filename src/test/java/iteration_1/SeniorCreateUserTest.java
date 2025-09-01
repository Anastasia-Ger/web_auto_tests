package iteration_1;

import comparison.ModelAssertions;
import generators.RandomData;
import generators.RandomModelGenerator;
import io.restassured.common.mapper.TypeRef;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SeniorCreateUserTest extends BaseTest {
    @Test
    public void adminCanCreateUserWithCorrectData() {

        // Create user
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

        // Get user's profile
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>
                (RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                        Endpoint.CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnedOk())
                .get();

        ModelAssertions.assertThatModels(createUserResponse, customerProfileResponse).match();

        // Get users' profiles by admin and check that user created
        List<GetUsersResponse> users = AdminSteps.adminGetUsers();
        String expectedUsername = createUserRequest.getUsername();
        softly.assertThat(users)
                .map(GetUsersResponse::getUsername)
                .contains(expectedUsername);

        softly.assertAll();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
           //     Arguments.of(" ", "Password33$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);

        // Get users' profiles by admin and check that user NOT created
        List<GetUsersResponse> users = AdminSteps.adminGetUsers();
        String expectedUsername = createUserRequest.getUsername();
        softly.assertThat(users)
                .map(GetUsersResponse::getUsername)
                .doesNotContain(expectedUsername);

        softly.assertAll();

    }
}
