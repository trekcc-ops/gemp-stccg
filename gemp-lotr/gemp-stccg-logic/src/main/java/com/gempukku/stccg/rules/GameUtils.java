package com.gempukku.stccg.rules;

import com.gempukku.stccg.adventure.InvalidSoloAdventureException;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.gamestate.GameState;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameUtils {

    public static String getFullName(CardBlueprint blueprint) { return blueprint.getFullName(); }
    public static String getCardLink(PhysicalCard card) { return card.getCardLink(); }
    public static String[] getAllPlayers(DefaultGame game) { return game.getAllPlayers(); }


    public static List<PhysicalCard> getRandomCards(List<? extends PhysicalCard> cards, int count) {
        List<PhysicalCard> randomizedCards = new ArrayList<>(cards);
        Collections.shuffle(randomizedCards, ThreadLocalRandom.current());

        return new LinkedList<>(randomizedCards.subList(0, Math.min(count, randomizedCards.size())));
    }

    public static String plural(int count, String noun) {
        StringBuilder sb = new StringBuilder();
        sb.append(count).append(" ").append(noun);
        if (count != 1)
            sb.append("s");
        return sb.toString();
    }

    public static String be(Collection<PhysicalCard> cards) {
        if (cards.size() > 1)
            return "are";
        return "is";
    }


    public static String getAppendedTextNames(Collection<? extends PhysicalCard> cards) {
        StringJoiner sj = new StringJoiner(", ");
        for (PhysicalCard card : cards)
            sj.add(card.getFullName());

        if (sj.length() == 0)
            return "none";
        else
            return sj.toString();
    }

    public static String getAppendedNames(Collection<? extends PhysicalCard> cards) {
        ArrayList<String> cardStrings = new ArrayList<>();
        for (PhysicalCard card : cards) {
            cardStrings.add(card.getCardLink());
        }

        if (cardStrings.isEmpty())
            return "none";

        return String.join(", ", cardStrings);
    }

    public static String SubstituteText(String text, ActionContext context)
    {
        String result = text;
        while (result.contains("{")) {
            int startIndex = result.indexOf("{");
            int endIndex = result.indexOf("}");
            String memory = result.substring(startIndex + 1, endIndex);
            if(context != null){
                String cardNames = getAppendedNames(context.getCardsFromMemory(memory));
                if(cardNames.equalsIgnoreCase("none")) {
                    try {
                        cardNames = context.getValueFromMemory(memory);
                    }
                    catch(IllegalArgumentException ex) {
                        cardNames = "none";
                    }
                }
                result = result.replace("{" + memory + "}", cardNames);
            }
        }

        return result;
    }

    // TODO - This method probably isn't doing what it should since the LotR elements were stripped out
    public static String getDeluxeCardLink(String blueprintId, CardBlueprint blueprint) {
        var cultureString = "";
        return "<div class='cardHint' value='" + blueprintId + "' + card_img_url='" + blueprint.getImageUrl() + "'>" +
                cultureString + (blueprint.isUnique() ? "·" : "") + " " + getFullName(blueprint) + "</div>";
    }

}