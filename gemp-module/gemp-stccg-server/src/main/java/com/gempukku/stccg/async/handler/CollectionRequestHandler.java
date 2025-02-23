package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionRequestHandler {

    public final void handleRequest(String uri, GempHttpRequest gempRequest, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        HttpRequest request = gempRequest.getRequest();
        if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            openPack(gempRequest, uri.substring(1), responseWriter, serverObjects);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getCollection(gempRequest, uri.substring(1), responseWriter, serverObjects);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getCollection(GempHttpRequest request, String collectionType, ResponseWriter responseWriter,
                               ServerObjects serverObjects)
            throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String filter = getQueryParameterSafely(queryDecoder, FormParameter.filter);
        int start = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.start));
        int count = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.count));

        User resourceOwner = request.user();

        CardCollection collection =
                serverObjects.getCollectionsManager().getPlayerCollection(resourceOwner, collectionType);

        if (collection == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        Iterable<GenericCardItem> items = collection.getAll();
        List<GenericCardItem> filteredResult = SortAndFilterCards.process(
                filter, items, serverObjects.getCardBlueprintLibrary(), serverObjects.getFormatLibrary());

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute(FormParameter.count.name(), String.valueOf(filteredResult.size()));
        doc.appendChild(collectionElem);

        for (int i = start; i < start + count; i++) {
            if (i >= 0 && i < filteredResult.size()) {
                GenericCardItem item = filteredResult.get(i);
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item, serverObjects);
                } else {
                    appendPackElement(doc, collectionElem, item, true, serverObjects);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        responseWriter.writeXmlResponseWithHeaders(doc, headers);
    }

    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item,
                                   ServerObjects serverObjects) throws Exception {
        Element card = doc.createElement("card");
        card.setAttribute("count", String.valueOf(item.getCount()));
        card.setAttribute("blueprintId", item.getBlueprintId());
        CardBlueprint blueprint = serverObjects.getCardBlueprintLibrary().getCardBlueprint(item.getBlueprintId());
        card.setAttribute("imageUrl", blueprint.getImageUrl());
        collectionElem.appendChild(card);
    }

    private void appendPackElement(Document doc, Node collectionElem, GenericCardItem item, boolean setContents,
                                   ServerObjects serverObjects) {
        String blueprintId = item.getBlueprintId();
        Element pack = doc.createElement("pack");
        pack.setAttribute("count", String.valueOf(item.getCount()));
        pack.setAttribute("blueprintId", blueprintId);
        if (setContents) {
            if (item.getType() == CardItemType.SELECTION) {
                List<GenericCardItem> contents = serverObjects.getProductLibrary().get(blueprintId)
                        .openPack(serverObjects.getCardBlueprintLibrary());
                StringBuilder contentsStr = new StringBuilder();
                for (GenericCardItem content : contents)
                    contentsStr.append(content.getBlueprintId()).append("|");
                contentsStr.delete(contentsStr.length() - 1, contentsStr.length());
                pack.setAttribute("contents", contentsStr.toString());
            }
        }
        collectionElem.appendChild(pack);
    }


    private void openPack(GempHttpRequest request, String collectionType, ResponseWriter responseWriter,
                          ServerObjects serverObjects) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request.getRequest());
        try {
            String selection = getFormParameterSafely(postDecoder, FormParameter.selection);
            String packId = getFormParameterSafely(postDecoder, FormParameter.pack);

            User resourceOwner = request.user();

            CollectionType collectionTypeObj = CollectionType.getCollectionTypeByCode(collectionType);
            if (collectionTypeObj == null)
                collectionTypeObj = serverObjects.getLeagueService().getCollectionTypeByCode(collectionType);
            CardCollection packContents = serverObjects.getCollectionsManager().openPackInPlayerCollection(
                    resourceOwner, collectionTypeObj, selection, serverObjects.getProductLibrary(), packId);

            if (packContents == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            Document doc = createNewDoc();
            Element collectionElem = doc.createElement("pack");
            doc.appendChild(collectionElem);

            for (GenericCardItem item : packContents.getAll()) {
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item, serverObjects);
                } else {
                    appendPackElement(doc, collectionElem, item, false, serverObjects);
                }
            }

            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    protected enum FormParameter {
        blueprintId, cardId, channelNumber, choiceId, collectionType,
        cost, count, decisionId, decisionValue,
        deck, deckContents, decklist, deckName, decks,
        desc, duration, filter, format, id, includeEvents, isInviteOnly, isPrivate, length, login, maxMatches,
        message, messageOfTheDay, name, notes, oldDeckName,
        ownedMin, pack, participantId, password, players, price, prizeMultiplier, product, reason, selection,
        seriesDuration, shutdown, start, startDay, targetFormat, timer
    }


    static String getQueryParameterSafely(QueryStringDecoder decoder, FormParameter parameter) {
        List<String> parameterValues = decoder.parameters().get(parameter.name());
        if (parameterValues != null && !parameterValues.isEmpty())
            return parameterValues.getFirst();
        else
            return null;
    }


    static String getFormParameterSafely(InterfaceHttpPostRequestDecoder decoder, FormParameter parameter)
            throws IOException {
        InterfaceHttpData data = decoder.getBodyHttpData(parameter.name());
        if (data == null)
            return null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            return attribute.getValue();
        } else {
            return null;
        }
    }


    static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }



}