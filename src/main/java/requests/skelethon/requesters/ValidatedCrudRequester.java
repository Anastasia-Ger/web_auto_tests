package requests.skelethon.requesters;

import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    CrudRequester crudRequester;
    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());

    }

    @Override
    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    // 🔹 method overloading
    public <R> R get(TypeRef<R> typeRef) {
        return crudRequester.get().extract().as(typeRef);
    }

    @Override
    public Object get(int id) {
        return null;
    }

    @Override
    public T update(BaseModel model) {

        return (T) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public Object delete(int id) {
        return null;
    }
}
