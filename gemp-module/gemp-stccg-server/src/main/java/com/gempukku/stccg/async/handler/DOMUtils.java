package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Set;

public final class DOMUtils {
    public static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    public static String getFormParameterSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName)
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

    protected static User getResourceOwnerSafely(HttpRequest request, String participantId, PlayerDAO playerDAO,
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

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !participantId.equals("null") &&
                !participantId.isEmpty()) {
            resourceOwner = playerDAO.getPlayer(participantId);
            if (resourceOwner == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
        return resourceOwner;
    }

    private static String getLoggedUser(HttpMessage request, LoggedUserHolder loggedUserHolder) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        HttpHeaders headers = request.headers();
        String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                String name = cookie.name();
                if ("loggedUser".equals(name)) {
                    String value = cookie.value();
                    if (value != null) {
                        return loggedUserHolder.getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isTest() {
        String test = System.getProperty("test");
        return Boolean.parseBoolean(test);
    }

}