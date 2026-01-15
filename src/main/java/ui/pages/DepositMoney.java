package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoney extends BasePage<DepositMoney>{
    private SelenideElement selectAccountField = $(Selectors.byClassName("account-selector"));
    private SelenideElement accountOptions = $(Selectors.byCssSelector("select.account-selector"));
    private SelenideElement enterAmountFld = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositBtn = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {return "/deposit";}

    public DepositMoney makeDeposit(String accountNumber, double amount) {
        selectAccountField.shouldBe(Condition.visible).click();
        accountOptions.selectOptionContainingText(accountNumber);
        enterAmountFld.sendKeys("" + amount);
        depositBtn.shouldBe(Condition.visible).shouldBe(Condition.enabled).click();

        return this;
    }

    // Method retrieves an amount of balance from account option in UI by AccountNumber
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
