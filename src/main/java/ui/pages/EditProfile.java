package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
@Getter

public class EditProfile extends BasePage<EditProfile>{
    private SelenideElement enterNewNameFld = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesBtn = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile updateName(String newName) {
        Selenide.refresh();
        userProfileMenu.should(Condition.exist).shouldBe(Condition.visible)
                .shouldBe(Condition.enabled).click();
        enterNewNameFld.shouldBe(Condition.visible).shouldBe(Condition.enabled).clear();
        enterNewNameFld.shouldBe(Condition.empty).shouldBe(Condition.exist).sendKeys(newName);
        saveChangesBtn.shouldBe(Condition.enabled).click();

        return this;
    }
    public String getUserProfileName() {
        return userProfileName.getText();
    }

}
