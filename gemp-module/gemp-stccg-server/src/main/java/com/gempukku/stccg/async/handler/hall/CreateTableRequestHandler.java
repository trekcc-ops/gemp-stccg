package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CreateTableRequestHandler implements UriRequestHandlerNew {
    private static final Logger LOGGER = LogManager.getLogger(CreateTableRequestHandler.class);
    private final String _desc;
    private final GameTimer _timer;
    private final boolean _isInviteOnly;
    private final boolean _isPrivate;
    private final String _deckName;
    private final String _format;

    CreateTableRequestHandler(
            @JsonProperty("format")
            String format,
            @JsonProperty("deckName")
            String deckName,
            @JsonProperty("timer")
            String timer,
            @JsonProperty("desc")
            String desc,
            @JsonProperty("isPrivate")
            boolean isPrivate,
            @JsonProperty("isInviteOnly")
            boolean isInviteOnly
    ) {
        _desc = desc.trim();
        _timer = GameTimer.ResolveTimer(timer);
        _isInviteOnly = isInviteOnly;
        _format = format;
        _deckName = deckName;
        _isPrivate = isPrivate;
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {

        //To prevent annoyance, super long glacial games are hidden from everyone except
        // the participants and admins.
        boolean isVisible = _timer != GameTimer.GLACIAL_TIMER;

        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        PlayerDAO playerDAO = serverObjects.getPlayerDAO();
        HallServer hallServer = serverObjects.getHallServer();

        if (_isInviteOnly) {
            String errorMessage = "";
            if (_desc.isEmpty()) {
                errorMessage = "Invite-only games must have your intended opponent in the description";
            } else if (_desc.equalsIgnoreCase(resourceOwner.getName())) {
                errorMessage = "Absolutely no playing with yourself!!  Private matches must be with someone else.";
            } else {
                try {
                    User player = playerDAO.getPlayer(_desc);
                    if (player == null)
                        throw new RuntimeException();
                } catch (RuntimeException ex) {
                    errorMessage = "Cannot find player '" + _desc + "'. " +
                            "Check your spelling and capitalization and ensure it is exact.";
                }
            }
            if (!errorMessage.isEmpty()) {
                responseWriter.writeXmlMarshalExceptionResponse(errorMessage);
                return;
            }
        }


        try {
            hallServer.createNewTable(
                    _format, resourceOwner, _deckName, _timer, _desc, _isInviteOnly, _isPrivate, !isVisible);
            responseWriter.writeXmlOkResponse();
        } catch (HallException e) {
            try {
                //try again assuming it's a new player with one of the default library decks selected
                hallServer.createNewTable(_format, resourceOwner, playerDAO.getPlayer("Librarian"),
                        _deckName, _timer, "(New Player) " + _desc, _isInviteOnly, _isPrivate, !isVisible);
                responseWriter.writeXmlOkResponse();
                return;
            } catch (HallException ignored) { }
            responseWriter.writeXmlMarshalExceptionResponse(e);
        } catch (Exception ex) {
            //This is a worthless error that doesn't need to be spammed into the log
            if (doNotIgnoreError(ex)) {
                LOGGER.error("Error response for {}", request.uri(), ex);
            }
            responseWriter.writeXmlMarshalExceptionResponse(
                    "Failed to create table. Please try again later.");
        }
    }

    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

}