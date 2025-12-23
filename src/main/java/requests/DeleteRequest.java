package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class DeleteRequest extends BaseModel {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public DeleteRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }
    public abstract ValidatableResponse delete(int userID);
}
