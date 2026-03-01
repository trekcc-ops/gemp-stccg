package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.DeckNotFoundException;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.GameSettings;
import com.gempukku.stccg.hall.GameTable;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JoinTableRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(JoinTableRequestHandler.class);
    private final String _deckName;
    private final String _tableId;
    private final HallServer _hallServer;
    private final AdminService _adminService;
    private final DeckDAO _deckDAO;
    private final CardBlueprintLibrary _cardLibrary;
    private final LeagueService _leagueService;
    private final GameServer _gameServer;
    JoinTableRequestHandler(
            @JsonProperty("deckName")
        String deckName,
            @JsonProperty("tableId")
        String tableId,
            @JacksonInject HallServer hallServer,
            @JacksonInject AdminService adminService,
            @JacksonInject CardBlueprintLibrary cardLibrary,
            @JacksonInject DeckDAO deckDAO,
            @JacksonInject LeagueService leagueService,
            @JacksonInject GameServer gameServer
            ) {
        _deckName = deckName;
        _tableId = tableId;
        _hallServer = hallServer;
        _adminService = adminService;
        _cardLibrary = cardLibrary;
        _deckDAO = deckDAO;
        _leagueService = leagueService;
        _gameServer = gameServer;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        if (_hallServer.isShutdown()) {
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");
        }

        GameTable gameTable = _hallServer.getActiveTableById(_tableId);

        if (gameTable == null) {
            throw new HallException("Table was already removed");
        } else if (gameTable.wasGameStarted()) {
            throw new HallException("Table is already taken or was removed");
        } else if (gameTable.hasPlayer(request.userName())) {
            throw new HallException("You can't play against yourself");
        }

        try {
            CardDeck deckFromData = _deckDAO.getDeckIfOwnedOrInLibrary(request.user(), _deckName, _adminService);
            CardDeck deckWithErrata =
                    _hallServer.validateDeckIsLegal(gameTable.getGameFormat(), _cardLibrary, deckFromData);
            GameSettings settings = gameTable.getGameSettings();
            _hallServer.validatePlayerForLeague(request.userName(), settings);
            gameTable.validateOpponentForLeague(request.userName(), _leagueService);
            _hallServer.joinTableAsPlayer(_leagueService, _gameServer, request.userName(),
                    deckWithErrata, gameTable);
            responseWriter.writeXmlOkResponse();
        } catch(DeckNotFoundException | HallException exp) {
            responseWriter.writeXmlMarshalExceptionResponse(exp);
        } catch(Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            responseWriter.writeXmlMarshalExceptionResponse(exp);
        }
    }

}