package com.gempukku.stccg.async.handler.game;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface GameRequestHandler {

    default void validateUserCanAccessGameState(User player, CardGameMediator mediator) throws PrivateInformationException {
        if (!player.hasType(User.Type.ADMIN) && !mediator.allowsSpectators() &&
                !mediator.hasPlayer(player.getName())) {
            throw new PrivateInformationException();
        }
    }

    default Set<Phase> getAutoPassPhases(GempHttpRequest request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.cookieHeader();
        if (!cookieHeader.isEmpty()) {
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
        Set<Phase> autoPassDefault = new HashSet<>();
        autoPassDefault.add(Phase.EXECUTE_ORDERS);
        return autoPassDefault;
    }

}