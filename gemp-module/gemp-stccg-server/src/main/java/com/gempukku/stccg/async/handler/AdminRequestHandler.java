package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.admin.AdminRequestHandlerNew;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

public class AdminRequestHandler extends AdminRequestHandlerNew {

    public final void handleRequest(String uri, GempHttpRequest gempRequest,
                                    ResponseWriter responseWriter, ServerObjects serverObjects) throws Exception {
        HttpRequest request = gempRequest.getRequest();
        String requestType = uri + request.method();
        switch(requestType) {
            case "/getDailyMessageGET":
                validateAdmin(gempRequest);
                getDailyMessage(request, responseWriter, serverObjects.getHallServer());
                break;
            case "/setDailyMessagePOST":
                validateAdmin(gempRequest);
                setDailyMessage(request, responseWriter, serverObjects.getHallServer());
                break;
            default:
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }


    private void getDailyMessage(HttpRequest request, ResponseWriter responseWriter, HallServer hallServer) {
        try(SelfClosingPostRequestDecoder ignored = new SelfClosingPostRequestDecoder(request)) {
            String dailyMessage = hallServer.getDailyMessage();
            if(dailyMessage != null)
                responseWriter.writeJsonResponse(HTMLUtils.replaceNewlines(dailyMessage));
        }
    }

    private void setDailyMessage(HttpRequest request, ResponseWriter responseWriter, HallServer hallServer) throws IOException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            hallServer.setDailyMessage(getFormParameterSafely(postDecoder, FormParameter.messageOfTheDay));
            responseWriter.writeJsonOkResponse();
        }
    }

    protected static class SelfClosingPostRequestDecoder extends HttpPostRequestDecoder implements AutoCloseable {

        SelfClosingPostRequestDecoder(HttpRequest request) {
            super(request);
        }

        @Override
        public void close() {
            destroy();
        }
    }

    protected enum FormParameter {
        availablePicks, blueprintId, cardId, channelNumber, choiceId, collectionType,
        cost, count, decisionId, decisionValue,
        deck, deckContents, decklist, deckName, decks,
        desc, duration, filter, format, id, includeEvents, isInviteOnly, isPrivate, length, login, maxMatches,
        message, messageOfTheDay, name, notes, oldDeckName,
        ownedMin, pack, participantId, password, players, price, prizeMultiplier, product, reason, selection,
        seriesDuration, shutdown, start, startDay, targetFormat, timer
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


}