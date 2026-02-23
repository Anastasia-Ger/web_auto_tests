package iteration_2_middle.ui;

import api.generators.RandomData;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.CreateUserSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.BasePage;
import ui.pages.DepositMoney;
import ui.pages.MakeTransfer;

import static org.assertj.core.api.Assertions.assertThat;


public class TransferTests extends BaseUiTest {
    private CreateUserRequest createSenderRequest;
    private CreateUserRequest createRecipientRequest;
    private int senderId;
    private int recipientId;
    private int senderAccountId;
    private String senderAccountNumber;
    private int recipientAccountId;
    private String recipientAccountNumber;
    private String recipientName;
    private String invalidRecipientAccountNumber;

    @BeforeEach
    void setUp() {
        // Preconditions:
        // Create sender and his account
        CreateUserSteps sender = CreateUserSteps.createUser();
        createSenderRequest = sender.getRequest();
        senderId = (int) sender.getUserId();
        CreateAccountResponse senderResponse = AdminSteps.createAccount(createSenderRequest);
        senderAccountId = senderResponse.getId();
        senderAccountNumber = senderResponse.getAccountNumber();

        // Accumulate balance for transfer check
        DepositRequest depositRequest = DepositRequest.builder()
                .id(senderAccountId)
                .balance(BankingTestData.MAX_DEPOSIT)
                .build();
        new CrudRequester(RequestSpecs.authAsUser(createSenderRequest.getUsername(),
                createSenderRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnedOk())
                .post(depositRequest);

        // Create recipient and his account
        CreateUserSteps recipient = CreateUserSteps.createUser();
        createRecipientRequest = recipient.getRequest();
        recipientId = (int) recipient.getUserId();
        CreateAccountResponse recipientResponse = AdminSteps.createAccount(createRecipientRequest);
        recipientAccountId = recipientResponse.getId();
        recipientAccountNumber = recipientResponse.getAccountNumber();

        // Create recipient name
        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name(RandomData.getName())
                .build();

        recipientName = new ValidatedCrudRequester<UpdateNameResponse>
                (RequestSpecs.authAsUser(createRecipientRequest.getUsername(),
                        createRecipientRequest.getPassword()),
                        Endpoint.UPDATE_CUSTOMER_NAME,
                        ResponseSpecs.requestReturnedOk())
                .update(updateNameRequest).getCustomer().getName();

        // Sender logs in UI
        authAsUser(createSenderRequest);

    }

    @AfterEach
        // Clean up test data
    void deleteUsers() {
        AdminSteps.deleteUser(senderId);
        AdminSteps.deleteUser(recipientId);
    }

    @Test
    public void userCanMakeTransferWithValidDataTest() {
        // Steps:
        // Sender makes a transfer
        new MakeTransfer().open().sendTransfer(senderAccountNumber, recipientName,
                        recipientAccountNumber, BankingTestData.TRANSFER_VALID_AMOUNT)
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.getMessage());

        // Check that balance changes in sender account in UI
        double senderActualBalance = new DepositMoney().open().getBalanceByAccountNumber(senderAccountNumber);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT - BankingTestData.TRANSFER_VALID_AMOUNT);
        BasePage.logout();

        // Check that balance changes in recipient account
        // Recipient logs in UI
        authAsUser(createRecipientRequest);
        // Get balance via Deposit page and assert
        double recipientActualBalance = new DepositMoney().open().getBalanceByAccountNumber(recipientAccountNumber);
        assertThat(recipientActualBalance).isEqualTo(BankingTestData.TRANSFER_VALID_AMOUNT);
        BasePage.logout();

        // API checks
        // Check that sender balance changes
        double actualSenderBalance = new UserSteps(createSenderRequest.getUsername(),
                createSenderRequest.getPassword()).getBalance(senderAccountNumber);

        assertThat(actualSenderBalance).isEqualTo(BankingTestData.MAX_DEPOSIT - BankingTestData.TRANSFER_VALID_AMOUNT);

        // Check that recipient balance changes on the amount of deposit
        double actualRecipientBalance = new UserSteps(createRecipientRequest.getUsername(),
                createRecipientRequest.getPassword()).getBalance(recipientAccountNumber);

        assertThat(actualRecipientBalance).isEqualTo(BankingTestData.TRANSFER_VALID_AMOUNT);

    }

    @Disabled
    @Test
    public void userCanNotTransferToInvalidAccount() {
        invalidRecipientAccountNumber = "ACC85";
        // Steps:
        // Sender makes a transfer
        new MakeTransfer().open().sendTransfer(senderAccountNumber, recipientName,
                        invalidRecipientAccountNumber, BankingTestData.TRANSFER_VALID_AMOUNT)
                .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER.getMessage());

        // Check that balance does not change in sender account in UI
        double senderActualBalance = new DepositMoney().open().getBalanceByAccountNumber(senderAccountNumber);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);
        BasePage.logout();

        // Check that balance does not change in recipient account in UI
        authAsUser(createRecipientRequest);
        double recipientActualBalance = new DepositMoney().open().getBalanceByAccountNumber(recipientAccountNumber);
        assertThat(recipientActualBalance).isZero();
        BasePage.logout();

    }

    @Test
    public void userCanNotTransferMoreThanMaximumAllowedAmount() {
        // Steps:
        // Sender makes a transfer
        new MakeTransfer().open().sendTransfer(senderAccountNumber, recipientName,
                        recipientAccountNumber, BankingTestData.TRANSFER_INVALID_UPPER)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        // Check that balance does not change in sender account
        double senderActualBalance = new DepositMoney().open().getBalanceByAccountNumber(senderAccountNumber);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);
        BasePage.logout();

        // Check that balance does not change in recipient account
        authAsUser(createRecipientRequest);
        double recipientActualBalance = new DepositMoney().open().getBalanceByAccountNumber(recipientAccountNumber);
        assertThat(recipientActualBalance).isZero();
        BasePage.logout();

        // API checks
        // Check that sender balance does not change
        double actualSenderBalance = new UserSteps(createSenderRequest.getUsername(),
                createSenderRequest.getPassword()).getBalance(senderAccountNumber);

        assertThat(actualSenderBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);

        // Check that recipient balance does not change
        double actualRecipientBalance = new UserSteps(createRecipientRequest.getUsername(),
                createRecipientRequest.getPassword()).getBalance(recipientAccountNumber);

        assertThat(actualRecipientBalance).isZero();
    }

    @Test
    public void userCanNotTransferWithEmptyRecipientName() {
        // Steps:
        // Sender makes a transfer
        new MakeTransfer().open().sendTransfer(senderAccountNumber, recipientAccountNumber,
                        BankingTestData.TRANSFER_VALID_AMOUNT)
                .checkAlertMessageAndAccept(BankAlert.RECIPIENT_NAME_DOES_NOT_MATCH_REGISTERED_NAME.getMessage());

        // Check that balance does not change in sender account
        double senderActualBalance = new DepositMoney().open().getBalanceByAccountNumber(senderAccountNumber);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);
        BasePage.logout();

        // Check that balance does not change in recipient account
        authAsUser(createRecipientRequest);
        double recipientActualBalance = new DepositMoney().open().getBalanceByAccountNumber(recipientAccountNumber);
        assertThat(recipientActualBalance).isZero();
        BasePage.logout();

    }

    @Test
    public void userCanNotTransferWithMissingRequiredFields() {
        // Steps:
        // Sender makes a transfer
        // Amount is missing
        new MakeTransfer().open().sendTransfer(senderAccountNumber, recipientName,
                        recipientAccountNumber)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        // Check that balance does not change in sender account in UI
        double senderActualBalance = new DepositMoney().open().getBalanceByAccountNumber(senderAccountNumber);
        assertThat(senderActualBalance).isEqualTo(BankingTestData.MAX_DEPOSIT);
        BasePage.logout();

        // Check that balance does not change in recipient account
        authAsUser(createRecipientRequest);
        double recipientActualBalance = new DepositMoney().open().getBalanceByAccountNumber(recipientAccountNumber);
        assertThat(recipientActualBalance).isZero();
        BasePage.logout();

    }
}
