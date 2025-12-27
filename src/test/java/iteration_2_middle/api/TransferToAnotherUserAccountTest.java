package iteration_2_middle.api;

import iteration_1.api.BaseTest;
import models.BankingTestData;
import models.CreateUserRequest;
import models.DepositRequest;
import models.TransferRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CreateUserSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferToAnotherUserAccountTest extends BaseTest {
    @Test
    public void userCanTransferToAnotherAccount() {

        // Create a user 1 - sender
        CreateUserSteps sender = CreateUserSteps.createUser();
        CreateUserRequest createUserRequest1 = sender.getRequest();
        int senderId = (int)sender.getUserId();

        // Create sender's account
        int senderAccountId = AdminSteps.createAccount(createUserRequest1).getId();


        // Sender performs 6 deposit transactions to accumulate enough balance for transfer limit check:
        // 10000 and boundary values - 9999, 10001
        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(BankingTestData.MAX_DEPOSIT)
                .build();

        for(int i = 0; i <6; i++) {
            new CrudRequester(RequestSpecs.authAsUser(createUserRequest1.getUsername(), createUserRequest1.getPassword()),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnedOk())
                    .post(depositRequest);
        }

        // Check balance from account info after 6 deposits
        double balanceAfterDeposit = UserSteps.getCustomerAccounts(createUserRequest1.getUsername(),
                createUserRequest1.getPassword()).get(0).getBalance();
        softly.assertThat(balanceAfterDeposit).isEqualTo(depositRequest.getBalance() * 6);


        // Create user 2 - receiver
        CreateUserSteps receiver = CreateUserSteps.createUser();
        CreateUserRequest createUserRequest2 = receiver.getRequest();
        int receiverId = (int)receiver.getUserId();

        // Create receiver's account
        int receiverAccountId = AdminSteps.createAccount(createUserRequest2).getId();

        // Get initial balance from  receiver's account info
        double initialBalance = UserSteps.getCustomerAccounts(createUserRequest2.getUsername(),
                createUserRequest2.getPassword()).get(0).getBalance();

        // Sender transfers money to receiver's account: lower boundary value - 9999
        TransferRequest transferRequest1 = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .recipientAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_VALID_LOWER)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest1.getUsername(),
                createUserRequest1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnedOk())
                .post(transferRequest1);


        // Get balance from  receiver's account info after transfer 1
       double balanceAfterTransfer1 = UserSteps.getCustomerAccounts(createUserRequest2.getUsername(),
                createUserRequest2.getPassword()).get(0).getBalance();

       softly.assertThat(balanceAfterTransfer1).isEqualTo(initialBalance + transferRequest1.getAmount());

       // Sender transfers money to receiver's account: valid amount 10000 (max allowed)
        TransferRequest transferRequest2 = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .recipientAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_VALID_BOUNDARY)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest1.getUsername(),
                createUserRequest1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnedOk())
                .post(transferRequest2);

        // Get balance from  receiver's account info after transfer 2
        double balanceAfterTransfer2 = UserSteps.getCustomerAccounts(createUserRequest2.getUsername(),
                createUserRequest2.getPassword()).get(0).getBalance();

        softly.assertThat(balanceAfterTransfer2).isEqualTo(balanceAfterTransfer1 + transferRequest2.getAmount());

        // Sender transfers money to receiver's account: upper boundary value 10001
        // Negative test
        TransferRequest transferRequest3 = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .recipientAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_INVALID_UPPER)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(createUserRequest1.getUsername(), createUserRequest1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestTransferOverLimit())
                .post(transferRequest3);

        // Get balance from  receiver's account info after transfer 3
        double balanceAfterTransfer3 = UserSteps.getCustomerAccounts(createUserRequest2.getUsername(),
                createUserRequest2.getPassword()).get(0).getBalance();

        softly.assertThat(balanceAfterTransfer3).isEqualTo(balanceAfterTransfer2);

        // Delete users
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.DELETE, ResponseSpecs.deleteUserOk(senderId))
                .delete(senderId);

        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.DELETE, ResponseSpecs.deleteUserOk(receiverId))
                .delete(receiverId);

        softly.assertAll();
    }
}
