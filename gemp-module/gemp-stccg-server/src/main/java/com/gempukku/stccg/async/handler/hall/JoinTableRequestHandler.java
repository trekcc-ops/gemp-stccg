package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameServer;
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
        User resourceOwner = request.user();

        try {
            _hallServer.joinTableAsPlayer(_tableId, resourceOwner, resourceOwner, _deckName, _cardLibrary, _deckDAO,
                    _leagueService, _gameServer);
            responseWriter.writeXmlOkResponse();
        } catch (HallException e) {
            try {
                //Try again assuming it's a new player using the default deck library decks
                User libraryOwner = _adminService.getPlayer("Librarian");
                _hallServer.joinTableAsPlayer(_tableId, resourceOwner, libraryOwner, _deckName, _cardLibrary, _deckDAO,
                        _leagueService, _gameServer);
                responseWriter.writeXmlOkResponse();
                return;
            } catch (HallException ex) {
                if (doNotIgnoreError(ex)) {
                    LOGGER.error("Error response for {}", request.uri(), ex);
                }
            } catch (Exception ex) {
                LOGGER.error("Additional error response for {}", request.uri(), ex);
                throw ex;
            }
            responseWriter.writeXmlMarshalExceptionResponse(e.getMessage());
        }
    }

    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }


}