package hdvtdev.academ.bots.telegram;

import hdvtdev.academ.Auth;
import hdvtdev.academ.Main;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Debug {

    public static void submitDebugCommand(Update update, String authToken) {

        Long chatId = update.getMessage().getChatId();
        String msg;
        if (update.getMessage().getText() == null) {
            msg = update.getMessage().getCaption();
        } else {
            msg = update.getMessage().getText();
        }

        String command = msg.split(" ")[0];
        List<String> args = Arrays.asList(msg.replace(command, "").split(" "));

        try {
            if ((Auth.isCodeValid(authToken, Integer.parseInt(args.getLast())))) {

                switch (command) {
                    case ":sysinfo" -> AcademTelegramBot.sendMessage(chatId, "test)");
                    case ":upload" -> uploadSchedule(update);
                    default -> AcademTelegramBot.sendMessage(chatId, "Неизвестная команда.");
                }



            } else {
                AcademTelegramBot.sendMessage(chatId, "Неверный код.");
            }
        } catch (NumberFormatException ignored) {
            AcademTelegramBot.sendMessage(chatId, "Неверный формат кода.");
        }




    }

    private static void uploadSchedule(Update update) {
        System.out.println("Starting upload schedule...");
        try {
            GetFile getFile = new GetFile(update.getMessage().getDocument().getFileId());

            org.telegram.telegrambots.meta.api.objects.File telegramFile = AcademTelegramBot.telegramClient.execute(getFile);

            String fileUrl = telegramFile.getFileUrl(AcademTelegramBot.token);
            downloadFile(fileUrl, String.valueOf(Main.pathToSchedule.resolve(update.getMessage().getDocument().getFileName())));

            AcademTelegramBot.sendMessage(update.getMessage().getChatId(), "Файл успешно загружен");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void downloadFile(String fileUrl, String localPath) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            File outputFile = new File(localPath);
            outputFile.getParentFile().mkdirs();

            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new Exception("Не удалось скачать файл: " + connection.getResponseMessage());
        }
    }




}
