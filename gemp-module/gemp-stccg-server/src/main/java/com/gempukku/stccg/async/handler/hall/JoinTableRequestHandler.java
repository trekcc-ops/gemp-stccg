package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JoinTableRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(JoinTableRequestHandler.class);
    private final String _deckName;
    private final String _tableId;
    JoinTableRequestHandler(
        @JsonProperty("deckName")
        String deckName,
        @JsonProperty("tableId")
        String tableId
    ) {
        _deckName = deckName;
        _tableId = tableId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        HallServer _hallServer = serverObjects.getHallServer();
        PlayerDAO _playerDAO = serverObjects.getPlayerDAO();

        try {
            _hallServer.joinTableAsPlayer(_tableId, resourceOwner, resourceOwner, _deckName);
            responseWriter.writeXmlOkResponse();
        } catch (HallException e) {
            try {
                //Try again assuming it's a new player using the default deck library decks
                User libraryOwner = _playerDAO.getPlayer("Librarian");
                _hallServer.joinTableAsPlayer(_tableId, resourceOwner, libraryOwner, _deckName);
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