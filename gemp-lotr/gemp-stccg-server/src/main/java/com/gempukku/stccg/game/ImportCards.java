package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.rules.GameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ImportCards {

    public List<CardCollection.Item> process(String rawDecklist, CardBlueprintLibrary cardLibrary) {
        Map<String, SubDeck> lackeySubDeckMap = new HashMap<>();
        for (SubDeck subDeck : SubDeck.values()) {
            lackeySubDeckMap.put(subDeck.getLackeyName() + ":", subDeck);
        }
        // Assumes formatting from Lackey txt files. "Draw deck" is not called out explicitly.
        SubDeck currentSubDeck = SubDeck.DRAW_DECK;

        List<CardCount> decklist = getDecklist(rawDecklist);

        List<CardCollection.Item> result = new ArrayList<>();
        for (CardCount cardCount : decklist) {
            SubDeck newSubDeck = lackeySubDeckMap.get(cardCount.name);
            if (newSubDeck != null) currentSubDeck = newSubDeck;
            else {
                /* TODO - Create a card name to blueprint ID map when the card blueprint library is created.
                    Accessing that map should be faster than iterating through the entire blueprint library
                    for every card.
                 */
                for (Map.Entry<String, CardBlueprint> cardBlueprint : cardLibrary.getBaseCards().entrySet()) {
                    String id = cardBlueprint.getKey();
                    if (isFromSupportedSet(id)) {
                        CardBlueprint blueprint = cardBlueprint.getValue();
                        if (exactNameMatch(blueprint, cardCount.name())) {
                            result.add(CardCollection.Item.createItem(id, cardCount.count(), currentSubDeck));
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean exactNameMatch(CardBlueprint blueprint, String title) {
        return blueprint != null
                && SortAndFilterCards.replaceSpecialCharacters(GameUtils.getFullName(blueprint).toLowerCase()).equals(title);
    }

    private boolean isFromSupportedSet(String id) {
        // TODO: Replace this logic with something that will check format-legal cards when importing decks
        try {
            int set = Integer.parseInt(id.split("_")[0]);
            return set >= 0;
        } catch (NumberFormatException exp) {
            return false;
        }
    }

    private final Pattern cardLinePattern = Pattern.compile("^(x?\\s*\\d+\\s*x?)?\\s*(.*?)\\s*(x?\\d+x?)?\\s*$");

    private List<CardCount> getDecklist(String rawDecklist) {
        int quantity;
        String cardLine;

        List<CardCount> result = new ArrayList<>();
        for (String line : rawDecklist.split("~")) {
            if (line.length() == 0)
                continue;

            line = line.toLowerCase();
            try {
                var matches = cardLinePattern.matcher(line);

                if(matches.matches()) {
                    if(!StringUtils.isEmpty(matches.group(1))) {
                        quantity = Integer.parseInt(matches.group(1).replaceAll("\\D+", ""));
                    }
                    else if(!StringUtils.isEmpty(matches.group(3))) {
                        quantity = Integer.parseInt(matches.group(3).replaceAll("\\D+", ""));
                    }
                    else {
                        quantity = 1;
                    }

                    cardLine = matches.group(2).trim();
                    result.add(new CardCount(SortAndFilterCards.replaceSpecialCharacters(cardLine).trim(), quantity));
                }
            } catch (Exception exp) {
                System.out.println("blah");
            }
        }
        return result;
    }

    private record CardCount(String name, int count) {
    }
}
