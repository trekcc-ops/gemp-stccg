package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;

final class DOMUtils {
    public static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    public static String getFormParameterSafely(InterfaceHttpPostRequestDecoder postRequestDecoder,
                                                String parameterName)
            throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        InterfaceHttpData data = postRequestDecoder.getBodyHttpData(parameterName);
        if (data == null)
            return null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            return attribute.getValue();
        } else {
            return null;
        }
    }

    static User getResourceOwnerSafely(HttpMessage request, String participantId, PlayerDAO playerDAO,
                                       LoggedUserHolder loggedUserHolder)
            throws HttpProcessingException {
        String loggedUser = loggedUserHolder.getLoggedUser(request);
        if (isTest() && loggedUser == null)
            loggedUser = participantId;

        if (loggedUser == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        User resourceOwner = playerDAO.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !"null".equals(participantId) &&
                !participantId.isEmpty()) {
            resourceOwner = playerDAO.getPlayer(participantId);
            if (resourceOwner == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
        return resourceOwner;
    }

    private static boolean isTest() {
        String test = System.getProperty("test");
        return Boolean.parseBoolean(test);
    }

}