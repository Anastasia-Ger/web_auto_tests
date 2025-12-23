package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class GetAccountRequester extends GetRequest<BaseModel>{
    public GetAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification){
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse get(BaseModel model) {
        return
                given()
                        .spec(requestSpecification)
                        .get("/api/v1/customer/accounts")
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }

}
