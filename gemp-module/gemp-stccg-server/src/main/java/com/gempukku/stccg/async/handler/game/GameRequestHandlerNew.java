package com.gempukku.stccg.async.handler.game;

import com.gempukku.stccg.common.filterable.Phase;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GameRequestHandlerNew {

    protected final Set<Phase> _autoPassDefault = new HashSet<>();
    protected final String _gameId;

    GameRequestHandlerNew(String gameId) {
        _autoPassDefault.add(Phase.EXECUTE_ORDERS);
        _gameId = gameId;
    }


    protected Set<Phase> getAutoPassPhases(HttpMessage request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("autoPassPhases".equals(cookie.name())) {
                    final String[] phases = cookie.value().split("0");
                    Set<Phase> result = new HashSet<>();
                    for (String phase : phases)
                        result.add(Phase.valueOf(phase.toUpperCase().replace(" ","_")));
                    return result;
                }
            }
            for (Cookie cookie : cookies) {
                if ("autoPass".equals(cookie.name()) && "false".equals(cookie.value()))
                    return Collections.emptySet();
            }
        }
        return _autoPassDefault;
    }

}