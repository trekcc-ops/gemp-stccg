package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.db.User;
import com.google.common.collect.Iterables;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.Map;

public class DeliveryRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final TransferDAO _transferDAO;

    public DeliveryRequestHandler(ServerObjects objects) {
        super(objects);
        _transferDAO = objects.getTransferDAO();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getDelivery(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getDelivery(HttpMessage request, ResponseWriter responseWriter) throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, null);
        Map<String, ? extends CardCollection> delivery = _transferDAO.consumeUndeliveredPackages(resourceOwner.getName());
        if (delivery == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        Document doc = createNewDoc();

        Element deliveryElem = doc.createElement("delivery");
        for (Map.Entry<String, ? extends CardCollection> collectionTypeItems : delivery.entrySet()) {
            String collectionType = collectionTypeItems.getKey();
            CardCollection items = collectionTypeItems.getValue();

            if (Iterables.size(items.getAll()) > 0) {
                Element collectionTypeElem = doc.createElement("collectionType");
                collectionTypeElem.setAttribute("name", collectionType);
                for (GenericCardItem item : items.getAll()) {
                    String blueprintId = item.getBlueprintId();
                    if (item.getType() == CardItemType.CARD) {
                        Element card = doc.createElement("card");
                        card.setAttribute("count", String.valueOf(item.getCount()));
                        card.setAttribute("blueprintId", blueprintId);
                        collectionTypeElem.appendChild(card);
                    } else {
                        Element pack = doc.createElement("pack");
                        pack.setAttribute("count", String.valueOf(item.getCount()));
                        pack.setAttribute("blueprintId", blueprintId);
                        collectionTypeElem.appendChild(pack);
                    }
                }
                deliveryElem.appendChild(collectionTypeElem);
            }
        }

        doc.appendChild(deliveryElem);

        responseWriter.writeXmlResponse(doc);
    }
}