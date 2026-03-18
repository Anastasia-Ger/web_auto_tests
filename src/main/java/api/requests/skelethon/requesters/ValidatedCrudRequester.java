package api.requests.skelethon.requesters;

import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import java.util.Arrays;
import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
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

    @Override
    public List<T> getAll(Class<?> clazz) {
        T[] array = (T[]) crudRequester.getAll(clazz).extract().as(clazz);
        return Arrays.asList(array);
    }
}
