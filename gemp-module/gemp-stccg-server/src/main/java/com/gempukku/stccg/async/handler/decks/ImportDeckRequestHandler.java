package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.SubDeck;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ImportDeckRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private final String _rawDeckList;

    ImportDeckRequestHandler(
            @JsonProperty(value = "deckList", required = true)
            String deckList
    ) {
        _rawDeckList = deckList;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        List<GenericCardItem> importResult = processImport(_rawDeckList, serverObjects.getCardBlueprintLibrary());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute("count", String.valueOf(importResult.size()));
        doc.appendChild(collectionElem);

        for (GenericCardItem item : importResult) {
            appendCardElement(doc, collectionElem, item, true, serverObjects.getCardBlueprintLibrary());
        }

        Map<String, String> headers = new HashMap<>();

        responseWriter.writeXmlResponseWithHeaders(doc, headers);
    }

    private static List<GenericCardItem> processImport(String rawDeckList, CardBlueprintLibrary cardLibrary) {
        Map<String, SubDeck> lackeySubDeckMap = new HashMap<>();
        for (SubDeck subDeck : SubDeck.values()) {
            lackeySubDeckMap.put(subDeck.getLackeyName() + ":", subDeck);
        }
        // Assumes formatting from Lackey txt files. "Draw deck" is not called out explicitly.
        SubDeck currentSubDeck = SubDeck.DRAW_DECK;

        List<GenericCardItem> result = new ArrayList<>();
        for (CardCount cardCount : getDecklist(rawDeckList)) {
            SubDeck newSubDeck = lackeySubDeckMap.get(cardCount.getName());
            if (newSubDeck != null) currentSubDeck = newSubDeck;
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
                                result.add(GenericCardItem.createItem(id, cardCount.count(), currentSubDeck));
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }

    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item,
                                   boolean setSubDeckAttribute, CardBlueprintLibrary library) throws Exception {
        Element card = doc.createElement("card");
        if (setSubDeckAttribute) {
            String subDeck = item.getSubDeckString();
            if (subDeck != null)
                card.setAttribute("subDeck", subDeck);
        }
        card.setAttribute("count", String.valueOf(item.getCount()));
        card.setAttribute("blueprintId", item.getBlueprintId());
        CardBlueprint blueprint = library.getCardBlueprint(item.getBlueprintId());
        card.setAttribute("imageUrl", blueprint.getImageUrl());
        collectionElem.appendChild(card);
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