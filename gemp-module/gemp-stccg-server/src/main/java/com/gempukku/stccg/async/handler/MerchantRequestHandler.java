package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardItem;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.merchant.BasicCardItem;
import com.gempukku.stccg.merchant.MerchantService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.net.HttpURLConnection;
import java.util.*;

@SuppressWarnings({"NestedMethodCall", "FeatureEnvy", "CallToNumericToString", "LongLine", "LawOfDemeter", "InstantiationOfUtilityClass"})
public class MerchantRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final SortAndFilterCards _sortAndFilterCards;
    private final MerchantService _merchantService;
    private final FormatLibrary _formatLibrary;

    private static final Logger LOGGER = LogManager.getLogger(MerchantRequestHandler.class);

    public MerchantRequestHandler(ServerObjects objects) {
        super(objects);

        _sortAndFilterCards = new SortAndFilterCards();
        _merchantService = objects.getMerchantService();
        _formatLibrary = objects.getFormatLibrary();

    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) 
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getMerchantOffers(request, responseWriter);
        } else if ("/buy".equals(uri) && request.method() == HttpMethod.POST) {
            buy(request, responseWriter);
        } else if ("/sell".equals(uri) && request.method() == HttpMethod.POST) {
            sell(request, responseWriter);
        } else if ("/tradeFoil".equals(uri) && request.method() == HttpMethod.POST) {
            tradeInFoil(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void tradeInFoil(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String blueprintId = getFormParameterSafely(postDecoder, FormParameter.blueprintId);

            User resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.tradeForFoil(resourceOwner, blueprintId);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                LOGGER.error("Error response for {}", request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void sell(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String blueprintId = getFormParameterSafely(postDecoder, FormParameter.blueprintId);
            int price = Integer.parseInt(getFormParameterSafely(postDecoder, FormParameter.price));

            User resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.merchantBuysCard(resourceOwner, blueprintId, price);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                LOGGER.error("Error response for {}", request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void buy(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String blueprintId = getFormParameterSafely(postDecoder, FormParameter.blueprintId);
            int price = Integer.parseInt(getFormParameterSafely(postDecoder, FormParameter.price));


            User resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _merchantService.merchantSellsCard(resourceOwner, blueprintId, price);
                responseWriter.writeHtmlResponse("OK");
            } catch (Exception exp) {
                LOGGER.error("Error response for {}", request.uri(), exp);
                responseWriter.writeXmlResponse(marshalException(exp));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void getMerchantOffers(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String filter = getQueryParameterSafely(queryDecoder, FormParameter.filter);
        int ownedMin = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.ownedMin));
        int start = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.start));
        int count = Integer.parseInt(getQueryParameterSafely(queryDecoder, FormParameter.count));

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode());

        Collection<BasicCardItem> cardItems = new HashSet<>();
        if (ownedMin <= 0) {
            cardItems.addAll(_merchantService.getSellableItems());
            final Iterable<GenericCardItem> items = collection.getAll();
            for (GenericCardItem item : items)
                cardItems.add(new BasicCardItem(item.getBlueprintId()));
        } else {
            for (GenericCardItem item : collection.getAll()) {
                if (item.getCount() >= ownedMin)
                    cardItems.add(new BasicCardItem(item.getBlueprintId()));
            }
        }

        List<BasicCardItem> filteredResult = _sortAndFilterCards.process(filter, cardItems, _cardBlueprintLibrary, _formatLibrary);

        Collection<CardItem> pageToDisplay = new ArrayList<>();
        for (int i = start; i < start + count; i++) {
            if (i >= 0 && i < filteredResult.size())
                pageToDisplay.add(filteredResult.get(i));
        }

        MerchantService.PriceGuarantee priceGuarantee = _merchantService.priceCards(resourceOwner);
        Map<String, Integer> buyPrices = priceGuarantee.getBuyPrices();
        Map<String, Integer> sellPrices = priceGuarantee.getSellPrices();

        Document doc = createNewDoc();

        Element merchantElem = doc.createElement("merchant");
        merchantElem.setAttribute("currency", String.valueOf(_collectionsManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));
        merchantElem.setAttribute(FormParameter.count.name(), String.valueOf(filteredResult.size()));
        doc.appendChild(merchantElem);

        for (CardItem cardItem : pageToDisplay) {
            String blueprintId = cardItem.getBlueprintId();

            Element elem;
            if (blueprintId.contains("_"))
                elem = doc.createElement("card");
            else
                elem = doc.createElement("pack");

            elem.setAttribute(FormParameter.count.name(), String.valueOf(collection.getItemCount(blueprintId)));
            if (blueprintId.contains("_") && !blueprintId.endsWith("*") && collection.getItemCount(blueprintId) >= 4)
                elem.setAttribute("tradeFoil", "true");
            elem.setAttribute("blueprintId", blueprintId);
            elem.setAttribute("imageUrl", _cardBlueprintLibrary.getCardBlueprint(blueprintId).getImageUrl());
            Integer buyPrice = buyPrices.get(blueprintId);
            if (buyPrice != null && collection.getItemCount(blueprintId) > 0)
                elem.setAttribute("buyPrice", buyPrice.toString());
            Integer sellPrice = sellPrices.get(blueprintId);
            if (sellPrice != null)
                elem.setAttribute("sellPrice", sellPrice.toString());
            merchantElem.appendChild(elem);
        }

        responseWriter.writeXmlResponse(doc);
    }

    private static Document marshalException(Exception e) throws ParserConfigurationException {
        Document doc = createNewDoc();

        Element error = doc.createElement("error");
        error.setAttribute("message", e.getMessage());
        doc.appendChild(error);
        return doc;
    }

}