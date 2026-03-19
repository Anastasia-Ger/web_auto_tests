package api.requests.skelethon.requesters;

import api.models.CreateUserResponse;
import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import api.specs.RequestSpecs;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    @Step("Post запрос на {endpoint} с телом {model}")
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("Post request to " + endpoint.getUrl(), () -> {
        var body = model == null ? "" : model;
        return
                given()
                        .spec(requestSpecification)
                        .body(body)
                        .post(endpoint.getUrl())
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
        });
    }

    @Override
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse get() {
        return
                given()
                        .spec(requestSpecification)
                        .get(endpoint.getUrl())
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }

    @Override
    @Step("GET запрос на {endpoint} с id {id}")
    public ValidatableResponse get(int id) {
        return
                given()
                        .spec(requestSpecification)
                        .get(endpoint.getUrl() + id)
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }

    @Override
    @Step("PUT запрос на {endpoint} с телом {model}")
    public ValidatableResponse update(BaseModel model) {
        return
                given()
                        .spec(requestSpecification)
                        .body(model)
                        .put(endpoint.getUrl())
                        .then()
                        .assertThat()
                        .spec(responseSpecification);

    }

    @Override
    @Step("DELETE запрос на {endpoint} с id {id}")
    public ValidatableResponse delete(int id) {

        return
                given()
                        .spec(requestSpecification)
                        .delete(endpoint.getUrl() + id)
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }
    @Override
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpecification);
    }
}
