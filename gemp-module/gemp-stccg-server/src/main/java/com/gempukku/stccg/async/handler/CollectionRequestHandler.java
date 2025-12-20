package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.packs.ProductLibrary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class CollectionRequestHandler implements UriRequestHandler {

    private final String _collectionType;
    private final int _start;
    private final int _count;
    private final String _filter;
    private final CollectionsManager _collectionsManager;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final ProductLibrary _productLibrary;

    CollectionRequestHandler(
            @JsonProperty("collectionType")
        String collectionType,
            @JsonProperty("start")
        int start,
            @JsonProperty("count")
        int count,
            @JsonProperty("filter")
        String filter,
            @JacksonInject CollectionsManager collectionsManager,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject ProductLibrary productLibrary) {
        _collectionType = collectionType;
        _start = start;
        _count = count;
        _filter = filter;
        _collectionsManager = collectionsManager;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
        _productLibrary = productLibrary;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        User resourceOwner = request.user();

        CardCollection collection = _collectionsManager.getPlayerCollectionWithLibrary(
                resourceOwner, _collectionType, _cardBlueprintLibrary);

        if (collection == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        Iterable<GenericCardItem> items = collection.getAll();
        List<GenericCardItem> filteredResult =
                SortAndFilterCards.process(_filter, items, _cardBlueprintLibrary, _formatLibrary);

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute("count", String.valueOf(filteredResult.size()));
        doc.appendChild(collectionElem);

        for (int i = _start; i < _start + _count; i++) {
            if (i >= 0 && i < filteredResult.size()) {
                GenericCardItem item = filteredResult.get(i);
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item);
                } else {
                    appendPackElement(doc, collectionElem, item);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        responseWriter.writeXmlResponseWithHeaders(doc, headers);
    }

    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item)
            throws Exception {
        Element card = doc.createElement("card");
        card.setAttribute("count", String.valueOf(item.getCount()));
        card.setAttribute("blueprintId", item.getBlueprintId());
        CardBlueprint blueprint = _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId());
        card.setAttribute("imageUrl", blueprint.getImageUrl());
        collectionElem.appendChild(card);
    }

    private void appendPackElement(Document doc, Node collectionElem, GenericCardItem item) {
        String blueprintId = item.getBlueprintId();
        Element pack = doc.createElement("pack");
        pack.setAttribute("count", String.valueOf(item.getCount()));
        pack.setAttribute("blueprintId", blueprintId);
        if (item.getType() == CardItemType.SELECTION) {
            List<GenericCardItem> contents = _productLibrary.get(blueprintId)
                    .openPack(_cardBlueprintLibrary);
            StringBuilder contentsStr = new StringBuilder();
            for (GenericCardItem content : contents)
                contentsStr.append(content.getBlueprintId()).append("|");
            contentsStr.delete(contentsStr.length() - 1, contentsStr.length());
            pack.setAttribute("contents", contentsStr.toString());
        }
        collectionElem.appendChild(pack);
    }


}