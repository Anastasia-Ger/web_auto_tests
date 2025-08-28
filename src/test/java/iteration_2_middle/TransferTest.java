package iteration_2_middle;

import iteration_1.BaseTest;
import models.BankingTestData;
import models.CreateUserRequest;
import models.DepositRequest;
import models.TransferRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CreateUserSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferTest extends BaseTest {
    private CreateUserSteps sender;
    private CreateUserSteps receiver;
    private int senderAccountId;
    private int receiverAccountId;

    @BeforeEach
    void setUp() {

        // Create sender
        sender = CreateUserSteps.createUser();
        CreateUserRequest createUserRequest1 = sender.getRequest();
        senderAccountId = AdminSteps.createAccount(createUserRequest1).getId();

        // Accumulate balance for transfer check:
        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(BankingTestData.MAX_DEPOSIT)
                .build();

        // 15000 needed for test, deposit 5000 → 3 times
        int depositsNeeded = BankingTestData.TEST_FUNDS_REQUIRED / BankingTestData.MAX_DEPOSIT;

        for (int depositNo = 0; depositNo < depositsNeeded; depositNo++) {
            new CrudRequester(RequestSpecs.authAsUser(createUserRequest1.getUsername(), createUserRequest1.getPassword()),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnedOk())
                    .post(depositRequest);
        }

        // Create receiver
        receiver = CreateUserSteps.createUser();
        CreateUserRequest createUserRequest2 = receiver.getRequest();

        receiverAccountId = AdminSteps.createAccount(createUserRequest2).getId();
    }

    @AfterEach
    void deleteUsers() {
        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.DELETE, ResponseSpecs.deleteUserOk((int) sender.getUserId()))
                .delete((int) sender.getUserId());

        new CrudRequester(RequestSpecs.adminSpec(),
                Endpoint.DELETE, ResponseSpecs.deleteUserOk((int) receiver.getUserId()))
                .delete((int) receiver.getUserId());
    }

    @Test
    public void userCanTransferValidLowerAmount() {

        // Get initial balance from  receiver's account info
        double initialBalance = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        // Sender transfers money to receiver's account: lower boundary value - 9999
        TransferRequest transferRequest1 = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_VALID_LOWER)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(sender.getRequest().getUsername(),
                sender.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnedOk())
                .post(transferRequest1);


        // Get balance from  receiver's account info after transfer 1
        double balanceAfterTransfer1 = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        softly.assertThat(balanceAfterTransfer1).isEqualTo(initialBalance + transferRequest1.getAmount());
    }

    @Test
    public void userCanTransferValidBoundary() {

        // Get initial balance from  receiver's account info
        double initialBalance = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        // Sender transfers money to receiver's account: valid amount 10000 (max allowed)
        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_VALID_BOUNDARY)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(sender.getRequest().getUsername(),
                sender.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnedOk())
                .post(transferRequest);

        // Get balance from  receiver's account info after transfer 2
        double balanceAfterTransfer = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        softly.assertThat(balanceAfterTransfer).isEqualTo(initialBalance + transferRequest.getAmount());
    }

    @Test
    public void userCanNotTransferInvalidAmount() {

        // Get initial balance from  receiver's account info
        double initialBalance = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        // Sender transfers money to receiver's account: upper boundary value 10001
        // Negative test
        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(BankingTestData.TRANSFER_INVALID_UPPER)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(sender.getRequest().getUsername(), sender.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestTransferOverLimit())
                .post(transferRequest);

        // Get balance from  receiver's account info after transfer 3
        double balanceAfterTransfer = UserSteps.getCustomerAccounts(receiver.getRequest().getUsername(),
                receiver.getRequest().getPassword()).get(0).getBalance();

        softly.assertThat(balanceAfterTransfer).isEqualTo(initialBalance);

    }
}



