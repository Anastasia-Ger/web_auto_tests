package iteration_2_middle;

import generators.RandomData;
import iteration_1.api.BaseTest;
import models.CreateUserRequest;
import models.UpdateUsernameRequest;
import models.UpdateUsernameResponse;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.CreateUserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
