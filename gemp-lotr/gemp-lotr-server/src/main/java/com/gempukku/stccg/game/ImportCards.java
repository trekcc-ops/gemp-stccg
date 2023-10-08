package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.cards.LotroCardBlueprint;
import com.gempukku.stccg.rules.GameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ImportCards {

    public List<CardCollection.Item> process(String rawDecklist, CardBlueprintLibrary cardLibrary) {
        List<CardCount> decklist = getDecklist(rawDecklist);

        List<CardCollection.Item> result = new ArrayList<>();
        for (CardCount cardCount : decklist) {
            for (Map.Entry<String, LotroCardBlueprint> cardBlueprint : cardLibrary.getBaseCards().entrySet()) {
                String id = cardBlueprint.getKey();
                if (isFromSupportedSet(id)) {
                    LotroCardBlueprint blueprint = cardBlueprint.getValue();
                    if (exactNameMatch(blueprint, cardCount.name())) {
                        result.add(CardCollection.Item.createItem(id, cardCount.count()));
                        break;
                    }
                }
            }
        }

        return result;
    }

    private boolean exactNameMatch(LotroCardBlueprint blueprint, String title) {
        return blueprint != null
                && SortAndFilterCards.replaceSpecialCharacters(GameUtils.getFullName(blueprint).toLowerCase()).equals(title);
    }

    private boolean isFromSupportedSet(String id) {
        try {
            int set = Integer.parseInt(id.split("_")[0]);
            return set < 20 || (set > 99 && set < 149);
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
