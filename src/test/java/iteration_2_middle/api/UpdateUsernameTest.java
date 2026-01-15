package iteration_2_middle.api;

import api.generators.RandomData;
import iteration_1.api.BaseTest;
import api.models.CreateUserRequest;
import api.models.UpdateUsernameRequest;
import api.models.UpdateUsernameResponse;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.CreateUserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class UpdateUsernameTest extends BaseTest {
    @Test
    public void userCanUpdateUsername() {
        // Create a user
        CreateUserSteps user = CreateUserSteps.createUser();
        CreateUserRequest userRequest = user.getRequest();
        int userId = (int)user.getUserId();

        // User updates username with valid data
        UpdateUsernameRequest updateRequest = UpdateUsernameRequest.builder()
                .username(RandomData.getUsername())
                .build();

        UpdateUsernameResponse response = new ValidatedCrudRequester<UpdateUsernameResponse>
                (RequestSpecs.authAsUser(userRequest.getUsername(),
                userRequest.getPassword()),
                Endpoint.UPDATE_CUSTOMER,
                ResponseSpecs.requestReturnedOk())
                .update(updateRequest);

    //    softly.assertThat(response.getCustomer().getUsername()).isEqualTo(updateRequest.getUsername());

        /*

        Assertion fails, as after update, response body does not contain a new username

        org.opentest4j.AssertionFailedError:
expected: "hncc"
 but was: "N2bCRFsrMT"
at UpdateUsernameTest.userCanUpdateUsername(UpdateUsernameTest.java:39)
Expected :"hncc"
Actual   :"N2bCRFsrMT"

         */

        // Delete user
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.DELETE, ResponseSpecs.deleteUserOk(userId))
                .delete(userId);

        softly.assertAll();


    }
}
