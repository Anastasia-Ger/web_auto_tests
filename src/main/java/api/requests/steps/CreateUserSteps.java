package api.requests.steps;

import api.generators.RandomModelGenerator;
import lombok.Value;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;


// Еще один способ создания юзера, когда нам надо получить userId для последующиего удаления юзера
// userId находится в теле ответа

@Value
public class CreateUserSteps {
    CreateUserRequest request;
    long userId;

    public static CreateUserSteps createUser() {

        CreateUserRequest request = RandomModelGenerator.generate(CreateUserRequest.class);


        CreateUserResponse response = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(request);


        return new CreateUserSteps(request, response.getId());
    }
}
