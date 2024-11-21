package hdvtdev.academ;


public class Main {

    private static String telegramApiToken;
    private static String discordApiToken;

    public static void main(String[] args) {

        if (args.length == 0 || args.length == 1) {
            if (args[0].equals("--help")) {
                System.out.println("help");
            } else System.err.println("Unknown argument");
            return;
        }

        if (args.length % 2 != 0) {
            System.err.println("Usage: <argument> <value>");
            return;
        }

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "--telegramApiToken" -> telegramApiToken = args[i + 1];
                case "--discordApiToken" -> discordApiToken = args[i + 1];
                default -> System.out.println("Unknown argument: " + args[i] + ", with value " + args[i + 1]);
            }
        }





    }




}
