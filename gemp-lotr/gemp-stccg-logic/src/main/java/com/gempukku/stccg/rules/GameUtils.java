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
    public static boolean isCurrentPlayer(DefaultGame game, String playerId) {
        return game.getGameState().getCurrentPlayerId().equals(playerId);
    }

    public static String getFullName(PhysicalCard card) {
        CardBlueprint blueprint = card.getBlueprint();
        return getFullName(blueprint);
    }

    public static String getFullName(CardBlueprint blueprint) {
        if (blueprint.getSubtitle() != null)
            return blueprint.getTitle() + ", " + blueprint.getSubtitle();
        return blueprint.getTitle();
    }

    public static String[] getOpponents(DefaultGame game, String playerId) {
        if (game.isSolo())
            throw new InvalidSoloAdventureException("Opponent requested");
        List<String> shadowPlayers = new LinkedList<>(game.getGameState().getPlayerOrder().getAllPlayers());
        shadowPlayers.remove(playerId);
        return shadowPlayers.toArray(new String[0]);
    }

    public static String[] getAllPlayers(DefaultGame game) {
        final GameState gameState = game.getGameState();
        final PlayerOrder playerOrder = gameState.getPlayerOrder();
        String[] result = new String[playerOrder.getPlayerCount()];

        final PlayOrder counterClockwisePlayOrder = playerOrder.getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
        int index = 0;

        String nextPlayer;
        while ((nextPlayer = counterClockwisePlayOrder.getNextPlayer()) != null) {
            result[index++] = nextPlayer;
        }
        return result;
    }

    public static List<PhysicalCard> getRandomCards(List<? extends PhysicalCard> cards, int count) {
        List<PhysicalCard> randomizedCards = new ArrayList<>(cards);
        Collections.shuffle(randomizedCards, ThreadLocalRandom.current());

        return new LinkedList<>(randomizedCards.subList(0, Math.min(count, randomizedCards.size())));
    }

    public static String s(Collection<PhysicalCard> cards) {
        if (cards.size() > 1)
            return "s";
        return "";
    }

    public static String s(int count) {
        if (count > 1)
            return "s";
        return "";
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

    public static String getCardLink(PhysicalCard card) {
        CardBlueprint blueprint = card.getBlueprint();
        return getCardLink(card.getBlueprintId(), blueprint);
    }

    public static String getCardLink(String blueprintId, CardBlueprint blueprint) {
        return "<div class='cardHint' value='" + blueprintId + "'>" + (blueprint.isUnique() ? "·" : "") + GameUtils.getFullName(blueprint) + "</div>";
    }

    public static String getAppendedTextNames(Collection<? extends PhysicalCard> cards) {
        StringJoiner sj = new StringJoiner(", ");
        for (PhysicalCard card : cards)
            sj.add(GameUtils.getFullName(card));

        if (sj.length() == 0)
            return "none";
        else
            return sj.toString();
    }

    public static String getAppendedNames(Collection<? extends PhysicalCard> cards) {
        ArrayList<String> cardStrings = new ArrayList<>();
        for (PhysicalCard card : cards) {
            cardStrings.add(GameUtils.getCardLink(card));
        }

        if (cardStrings.size() == 0)
            return "none";

        return String.join(", ", cardStrings);
    }

    public static String formatNumber(int effective, int requested) {
        if (effective != requested)
            return effective + "(out of " + requested + ")";
        else
            return String.valueOf(effective);
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
        return "<div class='cardHint' value='" + blueprintId + "'>" + cultureString
                + (blueprint.isUnique() ? "·" : "") + " " + getFullName(blueprint) + "</div>";
    }

}