package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.packs.ProductLibrary;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CollectionRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final LeagueService _leagueService;
    private final ProductLibrary _productLibrary;

    CollectionRequestHandler(ServerObjects objects) {
        super(objects);
        _leagueService = objects.getLeagueService();
        _productLibrary = objects.getProductLibrary();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter,
                                    String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getCollectionTypes(request, responseWriter);
        } else if (uri.startsWith("/import/") && request.method() == HttpMethod.GET) {
            importCollection(request, responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            openPack(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getCollection(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
    
    private void importCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        //noinspection SpellCheckingInspection
        List<GenericCardItem> importResult = processImport(
                getQueryParameterSafely(new QueryStringDecoder(request.uri()), FormParameter.decklist),
                _cardBlueprintLibrary
        );

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute(FormParameter.count.name(), String.valueOf(importResult.size()));
        doc.appendChild(collectionElem);

        for (GenericCardItem item : importResult) {
            appendCardElement(doc, collectionElem, item, true);
        }

        Map<String, String> headers = new HashMap<>();
        processDeliveryServiceNotification(request, headers);

        responseWriter.writeXmlResponse(doc, headers);
    }

    private void getCollection(HttpRequest request, String collectionType, ResponseWriter responseWriter)
            throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String filter = getQueryParameterSafely(queryDecoder, FormParameter.filter);
        int start = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.start));
        int count = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.count));

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType);

        if (collection == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        Iterable<GenericCardItem> items = collection.getAll();
        List<GenericCardItem> filteredResult =
                SortAndFilterCards.process(filter, items, _cardBlueprintLibrary, _serverObjects.getFormatLibrary());

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute(FormParameter.count.name(), String.valueOf(filteredResult.size()));
        doc.appendChild(collectionElem);

        for (int i = start; i < start + count; i++) {
            if (i >= 0 && i < filteredResult.size()) {
                GenericCardItem item = filteredResult.get(i);
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item, false);
                } else {
                    appendPackElement(doc, collectionElem, item, true);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        processDeliveryServiceNotification(request, headers);

        responseWriter.writeXmlResponse(doc, headers);
    }

    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item,
                                   boolean setSubDeckAttribute) throws Exception {
        Element card = doc.createElement("card");
        if (setSubDeckAttribute) {
            String subDeck = item.getSubDeckString();
            if (subDeck != null)
                card.setAttribute("subDeck", subDeck);
        }
        card.setAttribute("count", String.valueOf(item.getCount()));
        card.setAttribute("blueprintId", item.getBlueprintId());
        CardBlueprint blueprint = _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId());
        card.setAttribute("imageUrl", blueprint.getImageUrl());
        collectionElem.appendChild(card);
    }

    private void appendPackElement(Document doc, Node collectionElem, GenericCardItem item, boolean setContents) {
        String blueprintId = item.getBlueprintId();
        Element pack = doc.createElement("pack");
        pack.setAttribute("count", String.valueOf(item.getCount()));
        pack.setAttribute("blueprintId", blueprintId);
        if (setContents) {
            if (item.getType() == CardItemType.SELECTION) {
                List<GenericCardItem> contents = _productLibrary.GetProduct(blueprintId).openPack();
                StringBuilder contentsStr = new StringBuilder();
                for (GenericCardItem content : contents)
                    contentsStr.append(content.getBlueprintId()).append("|");
                contentsStr.delete(contentsStr.length() - 1, contentsStr.length());
                pack.setAttribute("contents", contentsStr.toString());
            }
        }
        collectionElem.appendChild(pack);
    }


    private void openPack(HttpRequest request, String collectionType, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
        String selection = getFormParameterSafely(postDecoder, FormParameter.selection);
        String packId = getFormParameterSafely(postDecoder, FormParameter.pack);

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CollectionType collectionTypeObj = CollectionType.getCollectionTypeByCode(collectionType);
        if (collectionTypeObj == null)
            collectionTypeObj = _leagueService.getCollectionTypeByCode(collectionType);
        CardCollection packContents = _collectionsManager.openPackInPlayerCollection(
                resourceOwner, collectionTypeObj, selection, _productLibrary, packId);

        if (packContents == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("pack");
        doc.appendChild(collectionElem);

        for (GenericCardItem item : packContents.getAll()) {
            if (item.getType() == CardItemType.CARD) {
                appendCardElement(doc, collectionElem, item, false);
            } else {
                appendPackElement(doc, collectionElem, item, false);
            }
        }

        responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    // This appears to be tied to "my cards", and "my trophies" selections in the Deck Builder,
    // which are now removed from the UI. Remove? If so, also remove getCollectionTypes() from comms.js.
    private void getCollectionTypes(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        User resourceOwner = getResourceOwner(request);
        Document doc = createNewDoc();
        Element collectionsElem = doc.createElement("collections");

        for (League league : _leagueService.getActiveLeagues()) {
            LeagueSeriesData seriesData = _leagueService.getCurrentLeagueSeries(league);
            if (seriesData != null && seriesData.isLimited() &&
                    _leagueService.isPlayerInLeague(league, resourceOwner)) {
                CollectionType collectionType = seriesData.getCollectionType();
                Element collectionElem = doc.createElement("collection");
                collectionElem.setAttribute("type", collectionType.getCode());
                collectionElem.setAttribute("name", collectionType.getFullName());
                collectionsElem.appendChild(collectionElem);
            }
        }
        doc.appendChild(collectionsElem);
        responseWriter.writeXmlResponse(doc);
    }

    private record CardCount(String name, int count) { }

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

    private static List<GenericCardItem> processImport(String rawDeckList, CardBlueprintLibrary cardLibrary) {
        Map<String, SubDeck> lackeySubDeckMap = new HashMap<>();
        for (SubDeck subDeck : SubDeck.values()) {
            lackeySubDeckMap.put(subDeck.getLackeyName() + ":", subDeck);
        }
        // Assumes formatting from Lackey txt files. "Draw deck" is not called out explicitly.
        SubDeck currentSubDeck = SubDeck.DRAW_DECK;

        List<GenericCardItem> result = new ArrayList<>();
        for (CardCount cardCount : getDecklist(rawDeckList)) {
            SubDeck newSubDeck = lackeySubDeckMap.get(cardCount.name);
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



}