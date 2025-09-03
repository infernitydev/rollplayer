package dev.infernity.rollplayer.i18n;

import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.List;

public class ListJoiner {
    public static String joinList(DiscordLocale ignoredLocale, List<String> list){
        return joinListEnglish(list);
    }

    private static String joinListEnglish(List<String> list) {
        if (list.size() < 3) {
            return String.join(" and ", list);
        }

        int lastIdx = list.size() - 1;

        return String.join(", ", list.subList(0, lastIdx)) +
                ", and " +
                list.get(lastIdx);
    }
}
