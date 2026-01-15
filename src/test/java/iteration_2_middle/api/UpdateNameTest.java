package iteration_2_middle.api;

import api.generators.RandomData;
import iteration_1.api.BaseTest;
import api.models.*;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.CreateUserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
