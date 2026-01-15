package api.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.models.UpdateUsernameRequest;

import static io.restassured.RestAssured.given;

public class UpdateUsernameRequester extends PutRequest<UpdateUsernameRequest>{
    public UpdateUsernameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse put(UpdateUsernameRequest model) {
        return
                given()
                        .spec(requestSpecification)
                        .body(model)
                        .put("/api/v1/customer/profile")
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }

}
