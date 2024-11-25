package hdvtdev.academ.bots.telegram;

import hdvtdev.academ.Auth;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcademTelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private final String token;
    private String authToken = null;
    private boolean insecure = false;
    private static TelegramClient telegramClient;
    private TelegramBotsLongPollingApplication botsApplication;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AcademTelegramBot(String token) {
        telegramClient = new OkHttpTelegramClient(token);
        this.token = token;
    }

    public AcademTelegramBot runImmediately() {
        run();
        return this;
    }

    public AcademTelegramBot addAuthToken(String authToken) {
        if (Auth.isSecretKeyValid(authToken)) {
            this.authToken = authToken;
        } else {
            insecure = true;
        }
        return this;
    }

    public void run() {
        CompletableFuture.runAsync(() -> {
            try {
                botsApplication = new TelegramBotsLongPollingApplication();
                botsApplication.registerBot(token, this);
                setBotCommands();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void shutdown() {
        try {
            botsApplication.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        executor.close();
    }

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            Messages.submitSlashCommandEvent(update);
        }

        if (update.hasCallbackQuery()) {
            Messages.submitCallbackQueryEvent(update);
        }

        if (update.hasMessage() && update.getMessage().hasText() && UserStateHandler.getUserState(update.getMessage().getChatId()).equals(UserState.SELECTING_GROUP)) {
           sendMessage(Messages.sendGroupSchedule(update.getMessage().getChatId(), update.getMessage().getText()));
        }

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith(":")) {
            if (insecure) {
                sendMessage(update.getMessage().getChatId(), "Bot currently in insecure mode.");
            } else {
                Debug.submitDebugCommand(update, authToken);
            }
        }


    }


    public static void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBotCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/schedule", "Расписание группы"));

        SetMyCommands setMyCommands = new SetMyCommands(commands);
        try {
            telegramClient.execute(setMyCommands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}