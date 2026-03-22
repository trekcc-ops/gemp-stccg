package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.packs.ProductLibrary;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.util.Objects;

public class OpenPackRequestHandler implements UriRequestHandler {

    private final String _selection;
    private final String _packId;
    private final CollectionType _collectionType;
    private final CollectionsManager _collectionsManager;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final ProductLibrary _productLibrary;

    OpenPackRequestHandler(
            @JsonProperty("selection")
        String selection,
            @JsonProperty("packId")
        String packId,
            @JsonProperty("collectionType")
        String collectionType,
            @JacksonInject LeagueService leagueService,
            @JacksonInject CollectionsManager collectionsManager,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject ProductLibrary productLibrary) {
        _selection = selection;
        _packId = packId;
        _collectionType = Objects.requireNonNullElse(
                CollectionType.getCollectionTypeByCode(collectionType),
                leagueService.getCollectionTypeByCode(collectionType)
        );
        _collectionsManager = collectionsManager;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _productLibrary = productLibrary;
    }

    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter)
            throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(gempRequest.getRequest());
        try {

            User resourceOwner = gempRequest.user();

            CardCollection packContents = _collectionsManager.openPackInPlayerCollection(
                    resourceOwner, _collectionType, _selection, _productLibrary, _packId, _cardBlueprintLibrary);

            if (packContents == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            Document doc = createNewDoc();
            Element collectionElem = doc.createElement("pack");
            doc.appendChild(collectionElem);

            for (GenericCardItem item : packContents) {
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item);
                } else {
                    appendPackElement(doc, collectionElem, item);
                }
            }

            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item) throws Exception {
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
        collectionElem.appendChild(pack);
    }

}