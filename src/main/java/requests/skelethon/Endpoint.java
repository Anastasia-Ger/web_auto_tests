package requests.skelethon;

import io.restassured.common.mapper.TypeRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class,
            null
    ),
    ADMIN_GET_USERS(
            "/admin/users",
            BaseModel.class,
            GetUsersResponse.class,
            new TypeRef<java.util.List<GetUsersResponse>>() {}
    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class,
            null
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetAccountResponse.class,
            new TypeRef<java.util.List<GetAccountResponse>>() {}
    ),
    CUSTOMER_PROFILE(
            "/customer/profile",
            BaseModel.class,
            CustomerProfileResponse.class,
            null
    ),
    UPDATE_CUSTOMER(
            "/customer/profile",
            UpdateUsernameRequest.class,
            UpdateUsernameResponse.class,
            null
    ),
    DELETE(
            "/admin/users/",
            BaseModel.class,
            BaseModel.class,
            null
    ),
    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class,
            null

    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class,
            null
    ),
    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class,
            null
    );



    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
    private final TypeRef<?> responseTypeRef;
}
