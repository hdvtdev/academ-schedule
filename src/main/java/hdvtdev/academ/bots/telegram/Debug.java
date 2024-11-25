package hdvtdev.academ.bots.telegram;

import hdvtdev.academ.Auth;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;

public class Debug {

    public static void submitDebugCommand(Update update, String authToken) {

        Long chatId = update.getMessage().getChatId();
        String msg = update.getMessage().getText();
        String command = msg.split(" ")[0];
        List<String> args = Arrays.asList(msg.replace(command, "").split(" "));

        try {
            if ((Auth.isCodeValid(authToken, Integer.parseInt(args.getLast())))) {

                switch (command) {
                    case ":sysinfo" -> AcademTelegramBot.sendMessage(chatId, "test)");
                    case ":baobab" -> {
                        StringBuilder sb = new StringBuilder();
                        args.forEach(s -> sb.append(s).append(" "));
                        sb.deleteCharAt(sb.length() - 1);
                        AcademTelegramBot.sendMessage(chatId, sb.toString());
                    }
                    default -> AcademTelegramBot.sendMessage(chatId, "Неизвестная команда.");
                }



            } else {
                AcademTelegramBot.sendMessage(chatId, "Неверный код.");
            }
        } catch (NumberFormatException ignored) {
            AcademTelegramBot.sendMessage(chatId, "Неверный формат кода.");
        }




    }




}
