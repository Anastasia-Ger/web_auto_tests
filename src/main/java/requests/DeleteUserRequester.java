package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class DeleteUserRequester extends DeleteRequest{
    public DeleteUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse delete(int userId) {
        return
                given()
                        .spec(requestSpecification)
                        .delete("/api/v1/admin/users/" + userId)
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }

}
