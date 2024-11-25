package hdvtdev.academ.bots.telegram;

import hdvtdev.*;
import hdvtdev.academ.AcademScheduleManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;


import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Messages {

    private static AcademScheduleManager oddScheduleManager;
    private static AcademScheduleManager evenScheduleManager;

    public static void setScheduleManagers(AcademScheduleManager oddScheduleManager, AcademScheduleManager evenScheduleManager) {
        Messages.oddScheduleManager = oddScheduleManager;
        Messages.evenScheduleManager = evenScheduleManager;
    }


    public static void submitSlashCommandEvent(Update update) {

        String msg = update.getMessage().getText();
        String command = msg.split(" ")[0];
        String[] args = msg.replace(command, "").split(" ");


        switch (command) {
            case "/schedule" -> {
                UserStateHandler.setUserState(update.getMessage().getChatId(), UserState.SELECTING_WEEKTYPE);
                AcademTelegramBot.sendMessage(weekTypeSelect(update));
            }
            default -> System.out.println("Unknown command " + command);
        }





    }

    public static void submitCallbackQueryEvent(Update update) {

        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callbackData) {
            case "odd", "even" -> {
                if (UserStateHandler.getUserState(chatId).equals(UserState.SELECTING_WEEKTYPE)) {
                    UserChoiceStorage.getUserChoice(chatId).setWeekType(callbackData);
                    UserStateHandler.setUserState(chatId, UserState.SELECTING_DAYOFWEEK);
                    AcademTelegramBot.sendMessage(dayOfWeekSelect(chatId));
                }
            }
            case "monday", "tuesday", "wednesday", "thursday", "friday", "saturday" -> {
                if (UserStateHandler.getUserState(chatId).equals(UserState.SELECTING_DAYOFWEEK)) {
                    UserChoiceStorage.getUserChoice(chatId).setDayOfWeek(callbackData);
                    UserStateHandler.setUserState(chatId, UserState.SELECTING_GROUP);
                    AcademTelegramBot.sendMessage(chatId, "Введите номер группы, например 216ис23");
                }
            }
        }

    }

    public static SendMessage sendGroupSchedule(Long chatId, String group) {

        AcademScheduleManager scheduleManager = WeekType.fromValue(UserChoiceStorage.getUserChoice(chatId).getWeekType()).equals(WeekType.ODD) ? oddScheduleManager : evenScheduleManager;
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        switch (UserChoiceStorage.getUserChoice(chatId).getDayOfWeek()) {
            case "tuesday" -> dayOfWeek = DayOfWeek.TUESDAY;
            case "wednesday" -> dayOfWeek = DayOfWeek.WEDNESDAY;
            case "thursday" -> dayOfWeek = DayOfWeek.THURSDAY;
            case "friday" -> dayOfWeek = DayOfWeek.FRIDAY;
            case "saturday" -> dayOfWeek = DayOfWeek.SATURDAY;
        }
        group = scheduleManager.findMostSimilarString(group);
        WeeklySchedule weeklySchedule = scheduleManager.getWeeklySchedule(group);
        String weekType = WeekType.of(0).equals(WeekType.ODD) ? "Нечётная, " : "Чётная, ";
        String dayOfWeekRus = "";
        switch (dayOfWeek) {
            case MONDAY -> dayOfWeekRus = "Понедельник, ";
            case TUESDAY -> dayOfWeekRus = "Вторник, ";
            case WEDNESDAY -> dayOfWeekRus = "Среда, ";
            case THURSDAY -> dayOfWeekRus = "Четверг, ";
            case FRIDAY -> dayOfWeekRus = "Пятница, ";
            case SATURDAY -> dayOfWeekRus = "Суббота, ";
        }
        DailySchedule dailySchedule = new DailySchedule(null, null);
        for (DailySchedule schedule : weeklySchedule.dailySchedules()) {
            if (schedule.dayOfWeek().equals(dayOfWeek)) {
                dailySchedule = schedule;
                break;
            }
        }


        SendMessage sendMessage = new SendMessage(chatId.toString(), weekType + dayOfWeekRus + weeklySchedule.id());
        List<InlineKeyboardButton> classes = new ArrayList<>();

        int index = 1;
        for (ClassData classData : dailySchedule.classData()) {
            if (classData != null) {
                InlineKeyboardButton button = new InlineKeyboardButton("Пара " + index + ": " + classData.className() + ", " + classData.teacher() + ", " + classData.classRoom());
                button.setCallbackData("null");
                classes.add(button);
            } else {
                InlineKeyboardButton button = new InlineKeyboardButton("Пара " + index + ": Нет пары");
                button.setCallbackData("null");
                classes.add(button);
            }
            index++;
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();

        classes.forEach(button -> rows.add(new InlineKeyboardRow(button)));

        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(rows));

        UserStateHandler.setUserState(chatId, UserState.DEFAULT);

        return sendMessage;
    }

    public static SendMessage dayOfWeekSelect(Long chatId) {

        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите день недели");

        List<InlineKeyboardButton> dayOfWeekButtons = new ArrayList<>();

        InlineKeyboardButton monday = new InlineKeyboardButton("Понедельник");
        monday.setCallbackData("monday");
        dayOfWeekButtons.add(monday);

        InlineKeyboardButton tuesday = new InlineKeyboardButton("Вторник");
        tuesday.setCallbackData("tuesday");
        dayOfWeekButtons.add(tuesday);

        InlineKeyboardButton wednesday = new InlineKeyboardButton("Среда");
        wednesday.setCallbackData("wednesday");
        dayOfWeekButtons.add(wednesday);

        InlineKeyboardButton thursday = new InlineKeyboardButton("Четверг");
        thursday.setCallbackData("thursday");
        dayOfWeekButtons.add(thursday);

        InlineKeyboardButton friday = new InlineKeyboardButton("Пятница");
        friday.setCallbackData("friday");
        dayOfWeekButtons.add(friday);

        InlineKeyboardButton saturday = new InlineKeyboardButton("Суббота");
        saturday.setCallbackData("saturday");
        dayOfWeekButtons.add(saturday);

        List<InlineKeyboardRow> rows = new ArrayList<>();

        dayOfWeekButtons.forEach(button -> rows.add(new InlineKeyboardRow(button)));

        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(rows));


        return sendMessage;

    }


    public static SendMessage weekTypeSelect(Update update) {


        String weekType = WeekType.of(Instant.now()).equals(WeekType.EVEN) ? "чётная" : "нечётная";

        SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), "Выберите неделю, текущая: " + weekType );

        InlineKeyboardButton buttonEven = new InlineKeyboardButton("Чётная");
        buttonEven.setCallbackData("even");

        InlineKeyboardButton buttonOdd = new InlineKeyboardButton("Нечётная");
        buttonOdd.setCallbackData("odd");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(buttonEven), new InlineKeyboardRow(buttonOdd)));

        message.setReplyMarkup(keyboardMarkup);

        return message;

    }










}
