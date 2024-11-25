package hdvtdev.academ;

import hdvtdev.ExcelScheduleManager;
import hdvtdev.ScheduleManager;
import hdvtdev.WeeklySchedule;
import hdvtdev.WeekType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AcademScheduleManager {

    private final Set<WeeklySchedule> weeklySchedules;
    private final Set<String> groups;

    public AcademScheduleManager(ExcelScheduleManager scheduleManager) {
        this.groups = scheduleManager.getGroups();
        this.weeklySchedules = scheduleManager.getWeeklySchedules();
        scheduleManager.close();
    }

    public WeeklySchedule getWeeklySchedule(String group) {

        group = findMostSimilarString(group);

        for (WeeklySchedule schedule : weeklySchedules) {
            if (schedule.id().equalsIgnoreCase(group)) {
                return schedule;
            }
        }
        return null;
    }

    public String findMostSimilarString(String input) {
        String mostSimilar = "";
        double maxSimilarity = -1;

        for (String str : groups) {
            double similarity = getSimilarityPercentage(str, input);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilar = str;
            }
        }

        return mostSimilar;
    }

    private static double getSimilarityPercentage(String str1, String str2) {
        int distance = calculateLevenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return (1 - (double) distance / maxLength) * 100;
    }

    private static int calculateLevenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }








}
