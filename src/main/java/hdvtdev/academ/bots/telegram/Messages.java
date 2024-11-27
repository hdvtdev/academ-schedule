package hdvtdev.academ.bots.telegram;

import hdvtdev.*;
import hdvtdev.academ.AcademScheduleManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


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
            default -> {
                System.out.println("default: " + callbackData);
                System.out.println("msg id: " + update.getCallbackQuery().getMessage().getMessageId());
                if (callbackData.startsWith("previous")) {
                    System.out.println("previous");
                    String weekType = callbackData.split("_")[1];
                    String chosenDayOfWeek = callbackData.split("_")[2];
                    String group = callbackData.split("_")[3];
                    DayOfWeek dayOfWeek = null;
                    switch (chosenDayOfWeek) {
                        case "monday" -> {
                            dayOfWeek = DayOfWeek.SATURDAY;
                            chosenDayOfWeek = "saturday";
                        }
                        case "tuesday" -> {
                            dayOfWeek = DayOfWeek.MONDAY;
                            chosenDayOfWeek = "monday";
                        }
                        case "wednesday" -> {
                            dayOfWeek = DayOfWeek.TUESDAY;
                            chosenDayOfWeek = "tuesday";
                        }
                        case "thursday" -> {
                            dayOfWeek = DayOfWeek.WEDNESDAY;
                            chosenDayOfWeek = "wednesday";
                        }
                        case "friday" -> {
                            dayOfWeek = DayOfWeek.THURSDAY;
                            chosenDayOfWeek = "thursday";
                        }
                        case "saturday" -> {
                            dayOfWeek = DayOfWeek.FRIDAY;
                            chosenDayOfWeek = "friday";
                        }
                    }
                    try {
                        AcademTelegramBot.telegramClient.execute(editGroupSchedule(chatId, group, weekType, dayOfWeek, chosenDayOfWeek, update.getCallbackQuery().getMessage().getMessageId()));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (callbackData.startsWith("next")) {
                    System.out.println("next");
                    String weekType = callbackData.split("_")[1];
                    String chosenDayOfWeek = callbackData.split("_")[2];
                    String group = callbackData.split("_")[3];
                    DayOfWeek dayOfWeek = null;
                    switch (chosenDayOfWeek) {
                        case "monday" -> {
                            dayOfWeek = DayOfWeek.TUESDAY;
                            chosenDayOfWeek = "tuesday";
                        }
                        case "tuesday" -> {
                            dayOfWeek = DayOfWeek.WEDNESDAY;
                            chosenDayOfWeek = "wednesday";
                        }
                        case "wednesday" -> {
                            dayOfWeek = DayOfWeek.THURSDAY;
                            chosenDayOfWeek = "thursday";
                        }
                        case "thursday" -> {
                            dayOfWeek = DayOfWeek.FRIDAY;
                            chosenDayOfWeek = "friday";
                        }
                        case "friday" -> {
                            dayOfWeek = DayOfWeek.SATURDAY;
                            chosenDayOfWeek = "saturday";
                        }
                        case "saturday" -> {
                            dayOfWeek = DayOfWeek.MONDAY;
                            chosenDayOfWeek = "monday";
                        }
                    }
                    try {
                        AcademTelegramBot.telegramClient.execute(editGroupSchedule(chatId, group, weekType, dayOfWeek, chosenDayOfWeek, update.getCallbackQuery().getMessage().getMessageId()));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

    public static EditMessageText editGroupSchedule(Long chatId, String group, String wkType, DayOfWeek dayOfWeek, String chosenDayOfWeek, Integer messageId) {

        AcademScheduleManager scheduleManager = WeekType.fromValue(wkType).equals(WeekType.ODD) ? oddScheduleManager : evenScheduleManager;

        group = scheduleManager.findMostSimilarString(group);
        WeeklySchedule weeklySchedule = scheduleManager.getWeeklySchedule(group);
        String weekType = WeekType.fromValue(wkType).equals(WeekType.ODD) ? "Нечётная, " : "Чётная, ";
        String dayOfWeekRus = switch (dayOfWeek) {
            case MONDAY -> "Понедельник, ";
            case TUESDAY -> "Вторник, ";
            case WEDNESDAY -> "Среда, ";
            case THURSDAY -> "Четверг, ";
            case FRIDAY -> "Пятница, ";
            case SATURDAY -> "Суббота, ";
            default -> "";
        };
        DailySchedule dailySchedule = new DailySchedule(null, null);
        for (DailySchedule schedule : weeklySchedule.dailySchedules()) {
            if (schedule.dayOfWeek().equals(dayOfWeek)) {
                dailySchedule = schedule;
                break;
            }
        }


        List<InlineKeyboardButton> classes = new ArrayList<>();
        int index = 1;
        for (ClassData classData : dailySchedule.classData()) {
            InlineKeyboardButton button;
            if (classData != null) {
                button = new InlineKeyboardButton(index + ". " + classData.className() + ", " + classData.teacher() + ", " + classData.classRoom());
            } else {
                button = new InlineKeyboardButton(index + ". Нет пары");
            }
            button.setCallbackData("null");
            classes.add(button);
            index++;
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();
        classes.forEach(button -> rows.add(new InlineKeyboardRow(button)));
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous_" + wkType + "_" + chosenDayOfWeek + "_" + group);

        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next_" + wkType + "_" + chosenDayOfWeek + "_" + group);

        rows.add(new InlineKeyboardRow(previous, next));
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup(rows);

        EditMessageText editMessage = new EditMessageText(weekType + dayOfWeekRus + weeklySchedule.id());
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setReplyMarkup(replyMarkup);

        UserStateHandler.setDefaultUserState(chatId);

        return editMessage;
    }

    public static SendMessage sendGroupSchedule(Long chatId, String group) {

        String wkType = UserChoiceStorage.getUserChoice(chatId).getWeekType();
        AcademScheduleManager scheduleManager = WeekType.fromValue(wkType).equals(WeekType.ODD) ? oddScheduleManager : evenScheduleManager;
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        String chosenDayOfWeek = UserChoiceStorage.getUserChoice(chatId).getDayOfWeek();
        switch (chosenDayOfWeek) {
            case "tuesday" -> dayOfWeek = DayOfWeek.TUESDAY;
            case "wednesday" -> dayOfWeek = DayOfWeek.WEDNESDAY;
            case "thursday" -> dayOfWeek = DayOfWeek.THURSDAY;
            case "friday" -> dayOfWeek = DayOfWeek.FRIDAY;
            case "saturday" -> dayOfWeek = DayOfWeek.SATURDAY;
        }
        group = scheduleManager.findMostSimilarString(group);
        WeeklySchedule weeklySchedule = scheduleManager.getWeeklySchedule(group);
        String weekType = WeekType.fromValue(UserChoiceStorage.getUserChoice(chatId).getWeekType()).equals(WeekType.ODD) ? "Нечётная, " : "Чётная, ";
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
            InlineKeyboardButton button;
            if (classData != null) {
                button = new InlineKeyboardButton(index + ". " + classData.className() + ", " + classData.teacher() + ", " + classData.classRoom());
            } else {
                button = new InlineKeyboardButton(index + ". Нет пары");
            }
            button.setCallbackData("null");
            classes.add(button);
            index++;
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();

        classes.forEach(button -> rows.add(new InlineKeyboardRow(button)));
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous_" + wkType + "_" + chosenDayOfWeek + "_" + group);

        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next_" + wkType + "_" + chosenDayOfWeek + "_" + group);

        rows.addLast(new InlineKeyboardRow(previous, next));
        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(rows));

        UserStateHandler.setDefaultUserState(chatId);

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
