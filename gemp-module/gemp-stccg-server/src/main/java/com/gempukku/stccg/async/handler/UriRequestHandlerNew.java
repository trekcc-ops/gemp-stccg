package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.draft.*;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginRequestHandler.class, name = "login"),
        @JsonSubTypes.Type(value = RegisterRequestHandler.class, name = "register")
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

}