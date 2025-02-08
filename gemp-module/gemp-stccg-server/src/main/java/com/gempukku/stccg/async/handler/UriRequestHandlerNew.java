package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.account.GameHistoryRequestHandler;
import com.gempukku.stccg.async.handler.account.PlayerStatsRequestHandler;
import com.gempukku.stccg.async.handler.chat.GetChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.PostChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.SendChatMessageRequestHandler;
import com.gempukku.stccg.async.handler.game.*;
import com.gempukku.stccg.async.handler.login.LoginRequestHandler;
import com.gempukku.stccg.async.handler.login.RegisterRequestHandler;
import com.gempukku.stccg.async.handler.server.ServerStatsRequestHandler;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CancelGameRequestHandler.class, name = "cancelGame"),
        @JsonSubTypes.Type(value = ConcedeGameRequestHandler.class, name = "concedeGame"),
        @JsonSubTypes.Type(value = DecisionResponseRequestHandler.class, name = "decisionResponse"),
        @JsonSubTypes.Type(value = GameCardInfoRequestHandler.class, name = "gameCardInfo"),
        @JsonSubTypes.Type(value = GameHistoryRequestHandler.class, name = "gameHistory"),
        @JsonSubTypes.Type(value = GetChatRequestHandler.class, name = "getChat"),
        @JsonSubTypes.Type(value = GetGameStateRequestHandler.class, name = "getGameState"),
        @JsonSubTypes.Type(value = LoginRequestHandler.class, name = "login"),
        @JsonSubTypes.Type(value = PlayerInfoRequestHandler.class, name = "playerInfo"),
        @JsonSubTypes.Type(value = PlayerStatsRequestHandler.class, name = "playerStats"),
        @JsonSubTypes.Type(value = PostChatRequestHandler.class, name = "postChat"),
        @JsonSubTypes.Type(value = RegisterRequestHandler.class, name = "register"),
        @JsonSubTypes.Type(value = ReplayRequestHandler.class, name = "replay"),
        @JsonSubTypes.Type(value = SendChatMessageRequestHandler.class, name = "sendChatMessage"),
        @JsonSubTypes.Type(value = ServerStatsRequestHandler.class, name = "serverStats"),
        @JsonSubTypes.Type(value = StartGameSessionRequestHandler.class, name = "startGameSession"),
        @JsonSubTypes.Type(value = UpdateGameStateRequestHandler.class, name = "updateGameState")
})
public interface UriRequestHandlerNew {
    void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                       ServerObjects serverObjects)
            throws Exception;

    default void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code % 400 < 100 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            log.debug("HTTP {} response for {}", code, uri);

        // record an HTTP 400
        else if(code == HttpURLConnection.HTTP_BAD_REQUEST || code % 500 < 100)
            log.error("HTTP code {} response for {}", code, uri, exp);
    }

    default Map<String, String> logUserReturningHeaders(String remoteIp, String login, ServerObjects objects)
            throws SQLException {
        objects.getPlayerDAO().updateLastLoginIp(login, remoteIp);

        String sessionId = objects.getLoggedUserHolder().logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }

    default User getResourceOwnerSafely(HttpMessage request, ServerObjects objects)
            throws HttpProcessingException {
        String loggedUser = getLoggedUser(request, objects);

        if (loggedUser == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        User resourceOwner = objects.getPlayerDAO().getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        return resourceOwner;
    }

    default String getLoggedUser(HttpMessage request, ServerObjects serverObjects) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("loggedUser".equals(cookie.name())) {
                    String value = cookie.value();
                    if (value != null) {
                        return serverObjects.getLoggedUserHolder().getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }



}