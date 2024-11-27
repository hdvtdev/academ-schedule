package hdvtdev.academ.bots.telegram;

import java.util.HashMap;
import java.util.Map;

@Deprecated(
        since = "0.0.2"
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
        since = "0.0.2"
)
 class UserChoice {

    private String group;
    private String messageId;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
