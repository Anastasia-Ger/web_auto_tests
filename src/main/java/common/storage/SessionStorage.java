package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    // единственный объект класса БЕЗ многопоточности

    //private static final SessionStorage INSTANCE = new SessionStorage();

    // ниже объект класса с учетом многопоточности (добавили ThreadLocal)
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private SessionStorage() {}
    public static void addUsers(List<CreateUserRequest> users) {
        for(CreateUserRequest user: users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }
    // Метод, кт позволяет обращаться к пользователю по индексу (это linked мапа, поэтому сохранен порядок добавления)
    // поэтому можем обратиться по индексу, а не по ключу

    /**
     * Возвращает объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number порядковый номер, начиная с 1 (а не с 0)
     * @return объект CreateUserRequest, соответствующий указанному порядковому номеру
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number-1);
    }
    // перегруженный метод, кт возвращает просто первый элемент (1-го юзера)
    public static CreateUserRequest getUser() {
        return getUser(1);
    }
    // возвращаем степы
    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number-1);
    }
    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
    }
}
