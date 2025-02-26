package com.gempukku.stccg.async.handler;

public class HTMLUtils {

    public static final String NEWLINE = "<br/>";

    public static final String HALL_WELCOME_MESSAGE = "You're now in the Game Hall, " +
            "use /help to get a list of available commands." + NEWLINE +
            "Don't forget to check out the new Discord chat integration! " +
            "Click the 'Switch to Discord' button in the lower right ---->";

    public static final String SINGLE_ELIMINATION_ON_DEMAND_PRIZES = "<div class='prizeHint' value='" +
            "2 wins - 2 boosters and a random promo, 1 win - 2 boosters, 0 wins - 1 booster'>(2+promo)-2-1</div>";

    public static String replaceNewlines(String message) {
        return message.replace("\n", NEWLINE);
    }


}