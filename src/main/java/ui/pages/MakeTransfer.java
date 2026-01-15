package ui.pages;

import api.models.BankingTestData;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class MakeTransfer extends BasePage<MakeTransfer>{
    private SelenideElement selectYourAccountFld = $(Selectors.byClassName("account-selector"));
    private SelenideElement accountOptions = $(Selectors.byCssSelector("select.account-selector"));
    private SelenideElement recipientNameFld = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountNumberFld = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement amountFld = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement confirmCheck = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferBtn = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {return "/transfer";}

    public MakeTransfer sendTransfer(String senderAccountNumber, String recipientName,
                             String recipientAccountNumber, double depositAmount) {

        selectYourAccountFld.click();
        accountOptions.selectOptionContainingText(senderAccountNumber);
        recipientNameFld.sendKeys(recipientName);
        recipientAccountNumberFld.sendKeys(recipientAccountNumber);
        amountFld.sendKeys("" + depositAmount);
        confirmCheck.click();
        sendTransferBtn.click();

        return this;
    }
    // Make transfer with empty recipient name field
    public MakeTransfer sendTransfer(String senderAccountNumber, String recipientAccountNumber, double depositAmount){

        selectYourAccountFld.click();
        accountOptions.selectOptionContainingText(senderAccountNumber);
        recipientAccountNumberFld.sendKeys(recipientAccountNumber);
        amountFld.sendKeys("" + depositAmount);
        confirmCheck.click();
        sendTransferBtn.click();

        return this;
    }
    // Make transfer with empty amount field
    public MakeTransfer sendTransfer(String senderAccountNumber, String recipientName,
                                     String recipientAccountNumber) {

        selectYourAccountFld.click();
        accountOptions.selectOptionContainingText(senderAccountNumber);
        recipientNameFld.sendKeys(recipientName);
        recipientAccountNumberFld.sendKeys(recipientAccountNumber);
        confirmCheck.click();
        sendTransferBtn.click();

        return this;
    }


    // Method retrieves an amount of balance from account option by AccountNumber
    public double getBalanceByAccountNumber(String accountNumber) {
        SelenideElement accountOption = accountOptions
                .$$("option")
                .findBy(Condition.text(accountNumber));

        String text = accountOption.getText();

        Pattern pattern = Pattern.compile("Balance:\\s*\\$(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(text);

        assertThat(matcher.find())
                .as("Balance should be present in option text")
                .isTrue();

        return Double.parseDouble(matcher.group(1));
    }



}
