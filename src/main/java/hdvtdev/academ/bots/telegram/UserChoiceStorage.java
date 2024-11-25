package hdvtdev.academ.bots.telegram;

import java.util.HashMap;
import java.util.Map;

@Deprecated(
        since = "0.0.2",
        forRemoval = true
)
public class UserChoiceStorage {

    private static final Map<Long, UserChoice> userChoices = new HashMap<>();

    public static UserChoice getUserChoice(Long chatId) {
        userChoices.putIfAbsent(chatId, new UserChoice());
        return userChoices.get(chatId);
    }

    public static void setUserChoice(Long chatId, UserChoice choice) {
        userChoices.put(chatId, choice);
    }
}

@Deprecated(
        since = "0.0.2",
        forRemoval = true
)
 class UserChoice {

    private String weekType;
    private String dayOfWeek;


    // Getters and setters
    public String getWeekType() {
        return weekType;
    }

    public void setWeekType(String weekType) {
        this.weekType = weekType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }


}
