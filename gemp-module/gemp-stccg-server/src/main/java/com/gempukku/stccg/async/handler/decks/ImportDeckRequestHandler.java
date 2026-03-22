package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.formats.FormatLibrary;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ImportDeckRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final String _rawDeckList;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;

    ImportDeckRequestHandler(
            @JsonProperty(value = "deckList", required = true)
            String deckList,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary) {
        _rawDeckList = deckList;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        Map<SubDeck, List<String>> importResult = processImport(_rawDeckList, _cardBlueprintLibrary);
        CardDeck deck = new CardDeck(importResult);
        JsonSerializedDeck serializedDeck = new JsonSerializedDeck(deck, _cardBlueprintLibrary, _formatLibrary);
        responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(serializedDeck));
    }

    private static Map<SubDeck, List<String>> processImport(String rawDeckList, CardBlueprintLibrary cardLibrary) {
        Map<SubDeck, List<String>> result = new HashMap<>();
        Map<String, SubDeck> lackeySubDeckMap = new HashMap<>();
        for (SubDeck subDeck : SubDeck.values()) {
            lackeySubDeckMap.put(subDeck.getLackeyName() + ":", subDeck);
        }
        // Assumes formatting from Lackey txt files. "Draw deck" is not called out explicitly.
        SubDeck currentSubDeck = SubDeck.DRAW_DECK;
        result.put(currentSubDeck, new ArrayList<>());

        for (CardCount cardCount : getDecklist(rawDeckList)) {
            SubDeck newSubDeck = lackeySubDeckMap.get(cardCount.getName());
            if (newSubDeck != null) {
                currentSubDeck = newSubDeck;
                result.put(newSubDeck, new ArrayList<>());
            }
            else {
                for (Map.Entry<String, CardBlueprint> cardBlueprint : cardLibrary.getBaseCards().entrySet()) {
                    String id = cardBlueprint.getKey();
                    try {
                        // If set is not a nonzero number, the card is not from a supported set
                        int set = Integer.parseInt(id.split("_")[0]);
                        if (set >= 0) {
                            CardBlueprint blueprint = cardBlueprint.getValue();

                            if (blueprint != null &&
                                    SortAndFilterCards.replaceSpecialCharacters(blueprint.getFullName().toLowerCase())
                                            .equals(cardCount.name())
                            ) {
                                for (int i = 0; i < cardCount.count(); i++) {
                                    result.get(currentSubDeck).add(id);
                                }
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }

    private static List<CardCount> getDecklist(String rawDeckList) {
        int quantity;
        String cardLine;

        List<CardCount> result = new ArrayList<>();
        for (String line : rawDeckList.split("~")) {
            if (line.isEmpty())
                continue;

            String line1 = line.toLowerCase();
            try {
                var matches = Pattern.compile("^(x?\\s*\\d+\\s*x?)?\\s*(.*?)\\s*(x?\\d+x?)?\\s*$").matcher(line1);

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

    public  record CardCount(String name, int count) {

        public String getName() {
            return name;
        }

    }


}