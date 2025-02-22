package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.account.GameHistoryRequestHandler;
import com.gempukku.stccg.async.handler.account.PlayerStatsRequestHandler;
import com.gempukku.stccg.async.handler.account.PlaytestReplaysRequestHandler;
import com.gempukku.stccg.async.handler.account.SetTesterFlagRequestHandler;
import com.gempukku.stccg.async.handler.admin.*;
import com.gempukku.stccg.async.handler.chat.GetChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.PostChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.SendChatMessageRequestHandler;
import com.gempukku.stccg.async.handler.decks.*;
import com.gempukku.stccg.async.handler.events.CurrentTournamentsRequestHandler;
import com.gempukku.stccg.async.handler.events.TournamentHistoryRequestHandler;
import com.gempukku.stccg.async.handler.game.*;
import com.gempukku.stccg.async.handler.hall.*;
import com.gempukku.stccg.async.handler.login.LoginRequestHandler;
import com.gempukku.stccg.async.handler.login.RegisterRequestHandler;
import com.gempukku.stccg.async.handler.server.ServerStatsRequestHandler;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddSealedLeagueRequestHandler.class, name = "addSealedLeague"),
        @JsonSubTypes.Type(value = BanUserRequestHandler.class, name = "banUser"),
        @JsonSubTypes.Type(value = BanUserTemporaryRequestHandler.class, name = "banUserTemporary"),
        @JsonSubTypes.Type(value = CancelGameRequestHandler.class, name = "cancelGame"),
        @JsonSubTypes.Type(value = ClearCacheRequestHandler.class, name = "clearCache"),
        @JsonSubTypes.Type(value = ConcedeGameRequestHandler.class, name = "concedeGame"),
        @JsonSubTypes.Type(value = CreateTableRequestHandler.class, name = "createTable"),
        @JsonSubTypes.Type(value = CurrentTournamentsRequestHandler.class, name = "currentTournaments"),
        @JsonSubTypes.Type(value = DecisionResponseRequestHandler.class, name = "decisionResponse"),
        @JsonSubTypes.Type(value = DeckFormatsRequestHandler.class, name = "deckFormats"),
        @JsonSubTypes.Type(value = DeckStatsRequestHandler.class, name = "deckStats"),
        @JsonSubTypes.Type(value = DeleteDeckRequestHandler.class, name = "deleteDeck"),
        @JsonSubTypes.Type(value = GameCardInfoRequestHandler.class, name = "gameCardInfo"),
        @JsonSubTypes.Type(value = GameHistoryRequestHandler.class, name = "gameHistory"),
        @JsonSubTypes.Type(value = GetChatRequestHandler.class, name = "getChat"),
        @JsonSubTypes.Type(value = GetErrataRequestHandler.class, name = "getErrata"),
        @JsonSubTypes.Type(value = GetHallRequestHandler.class, name = "getHall"),
        @JsonSubTypes.Type(value = GetGameStateRequestHandler.class, name = "getGameState"),
        @JsonSubTypes.Type(value = GetSetsRequestHandler.class, name = "getSets"),
        @JsonSubTypes.Type(value = ImportDeckRequestHandler.class, name = "importDeck"),
        @JsonSubTypes.Type(value = JoinQueueRequestHandler.class, name = "joinQueue"),
        @JsonSubTypes.Type(value = JoinTableRequestHandler.class, name = "joinTable"),
        @JsonSubTypes.Type(value = LeaveQueueRequestHandler.class, name = "leaveQueue"),
        @JsonSubTypes.Type(value = LeaveTableRequestHandler.class, name = "leaveTable"),
        @JsonSubTypes.Type(value = LeaveTournamentRequestHandler.class, name = "leaveTournament"),
        @JsonSubTypes.Type(value = ListLibraryDecksRequestHandler.class, name = "listLibraryDecks"),
        @JsonSubTypes.Type(value = ListUserDecksRequestHandler.class, name = "listUserDecks"),
        @JsonSubTypes.Type(value = LoginRequestHandler.class, name = "login"),
        @JsonSubTypes.Type(value = PlayerInfoRequestHandler.class, name = "playerInfo"),
        @JsonSubTypes.Type(value = PlayerStatsRequestHandler.class, name = "playerStats"),
        @JsonSubTypes.Type(value = PlaytestReplaysRequestHandler.class, name = "playtestReplays"),
        @JsonSubTypes.Type(value = PostChatRequestHandler.class, name = "postChat"),
        @JsonSubTypes.Type(value = PreviewSealedLeagueRequestHandler.class, name = "previewSealedLeague"),
        @JsonSubTypes.Type(value = RegisterRequestHandler.class, name = "register"),
        @JsonSubTypes.Type(value = ReloadCardLibraryRequestHandler.class, name = "reloadCardLibrary"),
        @JsonSubTypes.Type(value = RenameDeckRequestHandler.class, name = "renameDeck"),
        @JsonSubTypes.Type(value = ReplayRequestHandler.class, name = "replay"),
        @JsonSubTypes.Type(value = SaveDeckRequestHandler.class, name = "saveDeck"),
        @JsonSubTypes.Type(value = SendChatMessageRequestHandler.class, name = "sendChatMessage"),
        @JsonSubTypes.Type(value = ServerStatsRequestHandler.class, name = "serverStats"),
        @JsonSubTypes.Type(value = SetShutdownRequestHandler.class, name = "setShutdown"),
        @JsonSubTypes.Type(value = SetTesterFlagRequestHandler.class, name = "setTesterFlag"),
        @JsonSubTypes.Type(value = StartGameSessionRequestHandler.class, name = "startGameSession"),
        @JsonSubTypes.Type(value = TournamentHistoryRequestHandler.class, name = "tournamentHistory"),
        @JsonSubTypes.Type(value = UnBanUserRequestHandler.class, name = "unBanUser"),
        @JsonSubTypes.Type(value = UpdateGameStateRequestHandler.class, name = "updateGameState"),
        @JsonSubTypes.Type(value = UpdateHallRequestHandler.class, name = "updateHall")
})
public interface UriRequestHandlerNew {
    void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
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