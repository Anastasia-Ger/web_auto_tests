package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoneyBtn = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement makeATransferBtn = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));


    @Override
    public String url() {return "/dashboard";}


    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

}
