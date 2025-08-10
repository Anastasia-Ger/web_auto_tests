package iteration_2_middle;

import generators.RandomData;
import iteration_1.BaseTest;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferToAnotherUserAccountTest extends BaseTest {
    @Test
    public void userCanTransferToAnotherAccount() {

        // Create user 1
        CreateUserRequest user1Request = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();


        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(user1Request);


        // User 1 creates an account and gets account ID
        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");


        //User 1 performs 6 deposit transactions to accumulate enough balance for transfer limit check (10000)
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(5000)
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        new DepositMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        // Удали проверку аккаунта и баланса потом
        new GetAccountRequester(RequestSpecs.authAsUser(user1Request.getUsername(),
                user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .get(null);

        // Create user 2
        CreateUserRequest user2Request = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();


        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(user2Request);

        // User 2 creates an account and gets account ID
        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUser(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        // User 1 transfers money to user 2 account: lower boundary value - 9999
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(9999)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(transferRequest);

        // User 1 transfers money to user 2 account: valid amount 10000 (max allowed)
        TransferMoneyRequest transferRequestMaxallowed = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(10000)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnedOk())
                .post(transferRequestMaxallowed);

        // User 1 transfers money to user 2 account: upper boundary value 10001
        // Negative test
        TransferMoneyRequest transferRequestUpperBound = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(10001)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsBadRequestTransferOverLimit())
                .post(transferRequestUpperBound);


    }
}
