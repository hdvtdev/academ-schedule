package hdvtdev.academ.bots.telegram;

import java.util.HashMap;
import java.util.Map;

public class UserStateHandler {

    private static final Map<Long, UserState> userStates = new HashMap<>();

    public static void setUserState(Long userId, UserState state) {
        userStates.put(userId, state);
    }

    public static void setDefaultUserState(Long userId) {
        userStates.put(userId, UserState.DEFAULT);
    }

    public static UserState getUserState(Long userId) {
        return userStates.getOrDefault(userId, UserState.DEFAULT);
    }

    public static void removeUserState(Long userId) {
        userStates.remove(userId);
    }
}
