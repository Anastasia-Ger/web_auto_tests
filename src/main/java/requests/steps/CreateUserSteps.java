package requests.steps;

import generators.RandomModelGenerator;
import lombok.Value;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;


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
