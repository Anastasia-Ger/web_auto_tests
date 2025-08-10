package iteration_2_middle;

import generators.RandomData;
import models.CreateUserRequest;
import models.UpdateUsernameRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.UpdateUsernameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateUsernameTest {
    @Test
    public void userCanUpdateUsername() {

        // Create a user
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);


        // User updates username with valid data
        UpdateUsernameRequest updateRequest = UpdateUsernameRequest.builder()
                .username(RandomData.getUsername())
                .build();

        new UpdateUsernameRequester(RequestSpecs.authAsUser(userRequest.getUsername(),
                userRequest.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .put(updateRequest);

    }
}
