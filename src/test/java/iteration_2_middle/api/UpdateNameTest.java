package iteration_2_middle.api;

import generators.RandomData;
import iteration_1.api.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.CreateUserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateNameTest extends BaseTest {
    @Test
    public void CustomerCanUpdateName() {
        // Create a user
        CreateUserSteps user = CreateUserSteps.createUser();
        CreateUserRequest userRequest = user.getRequest();
        int userId = (int)user.getUserId();

        // User updates username with valid data
        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name(RandomData.getName())
                .build();

        UpdateNameResponse response = new ValidatedCrudRequester<UpdateNameResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(),
                userRequest.getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnedOk())
                .update(updateNameRequest);

            softly.assertThat(response.getCustomer().getName()).isEqualTo(updateNameRequest.getName());
    }
}
