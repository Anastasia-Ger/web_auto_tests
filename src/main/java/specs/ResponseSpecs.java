package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    // здесь тоже приватный конструктор, чтобы никто не мог создать экземпляры класса
    private ResponseSpecs() {}
    private  static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }
    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }
    public static ResponseSpecification requestReturnedOk() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }
    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String errorValue) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.equalTo(errorValue))
                .build();
    }
}
