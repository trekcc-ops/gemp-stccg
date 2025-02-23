package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;

public class OpenPackRequestHandler implements UriRequestHandler {

    private final String _selection;
    private final String _packId;
    private final String _collectionType;

    OpenPackRequestHandler(
        @JsonProperty("selection")
        String selection,
        @JsonProperty("packId")
        String packId,
        @JsonProperty("collectionType")
        String collectionType
    ) {
        _selection = selection;
        _packId = packId;
        _collectionType = collectionType;
    }

    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(gempRequest.getRequest());
        try {

            User resourceOwner = gempRequest.user();

            CollectionType collectionTypeObj = CollectionType.getCollectionTypeByCode(_collectionType);
            if (collectionTypeObj == null)
                collectionTypeObj = serverObjects.getLeagueService().getCollectionTypeByCode(_collectionType);
            CardCollection packContents = serverObjects.getCollectionsManager().openPackInPlayerCollection(
                    resourceOwner, collectionTypeObj, _selection, serverObjects.getProductLibrary(), _packId);

            if (packContents == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            Document doc = createNewDoc();
            Element collectionElem = doc.createElement("pack");
            doc.appendChild(collectionElem);

            for (GenericCardItem item : packContents.getAll()) {
                if (item.getType() == CardItemType.CARD) {
                    appendCardElement(doc, collectionElem, item, serverObjects);
                } else {
                    appendPackElement(doc, collectionElem, item);
                }
            }

            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } finally {
            postDecoder.destroy();
        }
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

    private void appendPackElement(Document doc, Node collectionElem, GenericCardItem item) {
        String blueprintId = item.getBlueprintId();
        Element pack = doc.createElement("pack");
        pack.setAttribute("count", String.valueOf(item.getCount()));
        pack.setAttribute("blueprintId", blueprintId);
        collectionElem.appendChild(pack);
    }

}