package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.*;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected static SelenideElement logoutBtn = $(Selectors.byText("\uD83D\uDEAA Logout"));
    protected SelenideElement userProfileMenu = $(Selectors.byClassName("profile-header"));
    protected SelenideElement userProfileName = $("span.user-name");

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {return Selenide.page(pageClass);}

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText().contains(bankAlert));
        alert.accept();
        Selenide.refresh();
        return (T) this;
    }
    public static LoginPage logout() {
        logoutBtn.shouldBe(Condition.visible).shouldBe(Condition.enabled).click();
        return new LoginPage();
    }
    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }
    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }
    // ElementCollection -> List<BaseElement>
    // protected чтобы был доступен каждому из наследников BasePage
    protected <T extends BaseElement>List<T> generatePageElements(ElementsCollection elementCollection, Function<SelenideElement, T> constructor) {
        return elementCollection.stream().map(constructor).toList();
    }

}
