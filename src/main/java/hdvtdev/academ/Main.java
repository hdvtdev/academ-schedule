package hdvtdev.academ;


import hdvtdev.ExcelScheduleManager;
import hdvtdev.academ.bots.telegram.AcademTelegramBot;
import hdvtdev.academ.bots.telegram.Messages;
import hdvtdev.academ.config.JsonConfigManager;
import schliph.CommandLineParser;


import java.nio.file.Path;

public class Main {



    private static String telegramApiToken;
    private static String discordApiToken;
    private static String authToken;
    public static Path pathToSchedule;

    public static void main(String[] args) {

        CommandLineParser cmd = new CommandLineParser(args);
        telegramApiToken = cmd.getAsOptional("--telegramApiToken").orElse("").toString();
        discordApiToken = cmd.getAsOptional("--discordApiToken").orElse("").toString();
        if (cmd.getAsOptional("--insecure").orElse("").toString().equals("false")) {
            authToken = cmd.getAsOptional("--authToken").orElse("").toString();
        }
        pathToSchedule = Path.of(cmd.getAsOptional("--schedulePath").orElse("").toString());
        cmd.close();

        Messages.setScheduleManagers(new AcademScheduleManager(new ExcelScheduleManager(pathToSchedule.resolve(Path.of("odd.xlsx")))), new AcademScheduleManager(new ExcelScheduleManager(pathToSchedule.resolve(Path.of("even.xlsx")))));

        AcademTelegramBot academTelegramBot = new AcademTelegramBot(telegramApiToken)
                .addAuthToken(authToken)
                .runImmediately();

        System.out.println("Bot started");




    }


}
