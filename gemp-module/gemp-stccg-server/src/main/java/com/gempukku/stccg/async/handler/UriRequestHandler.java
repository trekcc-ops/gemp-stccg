package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.account.GameHistoryRequestHandler;
import com.gempukku.stccg.async.handler.account.PlayerStatsRequestHandler;
import com.gempukku.stccg.async.handler.account.PlaytestReplaysRequestHandler;
import com.gempukku.stccg.async.handler.account.SetTesterFlagRequestHandler;
import com.gempukku.stccg.async.handler.admin.*;
import com.gempukku.stccg.async.handler.chat.GetChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.PostChatRequestHandler;
import com.gempukku.stccg.async.handler.chat.SendChatMessageRequestHandler;
import com.gempukku.stccg.async.handler.decks.*;
import com.gempukku.stccg.async.handler.events.*;
import com.gempukku.stccg.async.handler.game.*;
import com.gempukku.stccg.async.handler.hall.*;
import com.gempukku.stccg.async.handler.login.LoginRequestHandler;
import com.gempukku.stccg.async.handler.login.RegisterRequestHandler;
import com.gempukku.stccg.async.handler.server.ServerStatsRequestHandler;
import com.gempukku.stccg.service.AdminService;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddSealedLeagueRequestHandler.class, name = "addSealedLeague"),
        @JsonSubTypes.Type(value = BanUserRequestHandler.class, name = "banUser"),
        @JsonSubTypes.Type(value = BanUserMultipleRequestHandler.class, name = "banUserMultiple"),
        @JsonSubTypes.Type(value = BanUserTemporaryRequestHandler.class, name = "banUserTemporary"),
        @JsonSubTypes.Type(value = CancelGameRequestHandler.class, name = "cancelGame"),
        @JsonSubTypes.Type(value = ClearCacheRequestHandler.class, name = "clearCache"),
        @JsonSubTypes.Type(value = CollectionRequestHandler.class, name = "collection"),
        @JsonSubTypes.Type(value = ConcedeGameRequestHandler.class, name = "concedeGame"),
        @JsonSubTypes.Type(value = CreateTableRequestHandler.class, name = "createTable"),
        @JsonSubTypes.Type(value = CurrentLeaguesRequestHandler.class, name = "currentLeagues"),
        @JsonSubTypes.Type(value = CurrentTournamentsRequestHandler.class, name = "currentTournaments"),
        @JsonSubTypes.Type(value = DecisionResponseRequestHandler.class, name = "decisionResponse"),
        @JsonSubTypes.Type(value = DeckFormatsRequestHandler.class, name = "deckFormats"),
        @JsonSubTypes.Type(value = DeckStatsRequestHandler.class, name = "deckStats"),
        @JsonSubTypes.Type(value = DeleteDeckRequestHandler.class, name = "deleteDeck"),
        @JsonSubTypes.Type(value = FindMultipleAccountsRequestHandler.class, name = "findMultipleAccounts"),
        @JsonSubTypes.Type(value = GameCardInfoRequestHandler.class, name = "gameCardInfo"),
        @JsonSubTypes.Type(value = GameHistoryRequestHandler.class, name = "gameHistory"),
        @JsonSubTypes.Type(value = GetAvailableDraftPicksRequestHandler.class, name = "getAvailableDraftPicks"),
        @JsonSubTypes.Type(value = GetChatRequestHandler.class, name = "getChat"),
        @JsonSubTypes.Type(value = GetDailyMessageRequestHandler.class, name = "getDailyMessage"),
        @JsonSubTypes.Type(value = GetErrataRequestHandler.class, name = "getErrata"),
        @JsonSubTypes.Type(value = GetHallRequestHandler.class, name = "getHall"),
        @JsonSubTypes.Type(value = GetGameStateRequestHandler.class, name = "getGameState"),
        @JsonSubTypes.Type(value = GetLeagueRequestHandler.class, name = "getLeague"),
        @JsonSubTypes.Type(value = GetSetsRequestHandler.class, name = "getSets"),
        @JsonSubTypes.Type(value = HallStatusRequestHandler.class, name = "hallStatus"),
        @JsonSubTypes.Type(value = ImportDeckRequestHandler.class, name = "importDeck"),
        @JsonSubTypes.Type(value = JoinLeagueRequestHandler.class, name = "joinLeague"),
        @JsonSubTypes.Type(value = JoinQueueRequestHandler.class, name = "joinQueue"),
        @JsonSubTypes.Type(value = JoinTableRequestHandler.class, name = "joinTable"),
        @JsonSubTypes.Type(value = LeagueAdminConstructedRequestHandler.class, name = "leagueAdminConstructed"),
        @JsonSubTypes.Type(value = LeagueAdminDraftRequestHandler.class, name = "leagueAdminDraft"),
        @JsonSubTypes.Type(value = LeaveQueueRequestHandler.class, name = "leaveQueue"),
        @JsonSubTypes.Type(value = LeaveTableRequestHandler.class, name = "leaveTable"),
        @JsonSubTypes.Type(value = LeaveTournamentRequestHandler.class, name = "leaveTournament"),
        @JsonSubTypes.Type(value = ListLibraryDecksRequestHandler.class, name = "listLibraryDecks"),
        @JsonSubTypes.Type(value = ListUserDecksRequestHandler.class, name = "listUserDecks"),
        @JsonSubTypes.Type(value = LoginRequestHandler.class, name = "login"),
        @JsonSubTypes.Type(value = MakeDraftPickRequestHandler.class, name = "makeDraftPick"),
        @JsonSubTypes.Type(value = OpenPackRequestHandler.class, name = "openPack"),
        @JsonSubTypes.Type(value = PlayerInfoRequestHandler.class, name = "playerInfo"),
        @JsonSubTypes.Type(value = PlayerStatsRequestHandler.class, name = "playerStats"),
        @JsonSubTypes.Type(value = PlaytestReplaysRequestHandler.class, name = "playtestReplays"),
        @JsonSubTypes.Type(value = PostChatRequestHandler.class, name = "postChat"),
        @JsonSubTypes.Type(value = PreviewSealedLeagueRequestHandler.class, name = "previewSealedLeague"),
        @JsonSubTypes.Type(value = RegisterRequestHandler.class, name = "register"),
        @JsonSubTypes.Type(value = ReloadCardLibraryRequestHandler.class, name = "reloadCardLibrary"),
        @JsonSubTypes.Type(value = RenameDeckRequestHandler.class, name = "renameDeck"),
        @JsonSubTypes.Type(value = ReplayRequestHandler.class, name = "replay"),
        @JsonSubTypes.Type(value = ResetUserPasswordRequestHandler.class, name = "resetUserPassword"),
        @JsonSubTypes.Type(value = SaveDeckRequestHandler.class, name = "saveDeck"),
        @JsonSubTypes.Type(value = SendChatMessageRequestHandler.class, name = "sendChatMessage"),
        @JsonSubTypes.Type(value = ServerStatsRequestHandler.class, name = "serverStats"),
        @JsonSubTypes.Type(value = SetDailyMessageRequestHandler.class, name = "setDailyMessage"),
        @JsonSubTypes.Type(value = SetShutdownRequestHandler.class, name = "setShutdown"),
        @JsonSubTypes.Type(value = SetTesterFlagRequestHandler.class, name = "setTesterFlag"),
        @JsonSubTypes.Type(value = StartGameSessionRequestHandler.class, name = "startGameSession"),
        @JsonSubTypes.Type(value = TournamentHistoryRequestHandler.class, name = "tournamentHistory"),
        @JsonSubTypes.Type(value = UnBanUserRequestHandler.class, name = "unBanUser"),
        @JsonSubTypes.Type(value = UpdateGameStateRequestHandler.class, name = "updateGameState"),
        @JsonSubTypes.Type(value = UpdateHallRequestHandler.class, name = "updateHall")
})
public interface UriRequestHandler {
    void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
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

    default Map<String, String> logUserReturningHeaders(String remoteIp, String login,
                                                        AdminService adminService)
            throws SQLException {
        adminService.updateLastLoginIp(login, remoteIp);

        String sessionId = adminService.logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }


    default Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }


}