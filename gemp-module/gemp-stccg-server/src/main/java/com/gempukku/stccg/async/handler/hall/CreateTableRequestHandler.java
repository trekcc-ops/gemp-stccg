package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.database.UserNotFoundException;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.GameSettings;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueSeries;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CreateTableRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(CreateTableRequestHandler.class);
    private final String _desc;
    private final GameTimer _timer;
    private final boolean _isInviteOnly;
    private final GameFormat _format;
    private final LeagueSeries _series;
    private final boolean _isPrivate;
    private final String _deckName;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final DeckDAO _deckDAO;
    private final HallServer _hallServer;
    private final AdminService _adminService;
    private final League _league;
    private String _hallExceptionMessage;
    private final GameServer _gameServer;
    private final LeagueService _leagueService;
    private final boolean _isVisible;
    private final FormatLibrary _formatLibrary;
    private boolean _tableCreationFailed;
    private boolean _tableJoinFailed;

    CreateTableRequestHandler(
            @JsonProperty("format")
            String rawFormat,
            @JsonProperty("deckName")
            String deckName,
            @JsonProperty("timer")
            String rawTimer,
            @JsonProperty("desc")
            String rawDesc,
            @JsonProperty("isPrivate")
            boolean isPrivate,
            @JsonProperty("isInviteOnly")
            boolean isInviteOnly,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject DeckDAO deckDAO,
            @JacksonInject HallServer hallServer,
            @JacksonInject LeagueService leagueService,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject AdminService adminService,
            @JacksonInject GameServer gameServer) {
        _isInviteOnly = isInviteOnly;
        _deckName = deckName;
        _isPrivate = isPrivate;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _deckDAO = deckDAO;
        _hallServer = hallServer;
        _adminService = adminService;
        _gameServer = gameServer;
        _leagueService = leagueService;

        League league = null;
        LeagueSeries series = null;
        GameFormat format = formatLibrary.getHallFormats().get(rawFormat);
        GameTimer gameTimer = GameTimer.ResolveTimer(rawTimer);
        String desc = rawDesc.trim();

        if (format == null) {
            // Maybe it's a league format?
            try {
                league = leagueService.getLeagueById(rawFormat);
                series = leagueService.getCurrentLeagueSeries(league);
                if (series == null) {
                    _hallExceptionMessage = "There is no ongoing series for that league";
                } else if (_isInviteOnly) {
                    _hallExceptionMessage = "League games cannot be invite-only";
                } else if (_isPrivate) {
                    _hallExceptionMessage = "League games cannot be private";
                } else {
                    //Don't want people getting around the anonymity for leagues.
                    desc = "";
                    format = series.getFormat();
                    gameTimer = GameTimer.COMPETITIVE_TIMER;
                }
            } catch(LeagueNotFoundException ignored) {

            }
        }
        // It's not a normal format and also not a league one
        if (format == null) {
            _hallExceptionMessage = "This format is not supported: " + rawFormat;
        }
        _isVisible = gameTimer != GameTimer.GLACIAL_TIMER;
        _league = league;
        _desc = desc;
        _series = series;
        _timer = gameTimer;
        _format = format;
        _formatLibrary = formatLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        User resourceOwner = request.user();

        if (_hallServer.isShutdown()) {
            responseWriter.writeXmlMarshalExceptionResponse("Server is in shutdown mode. " +
                    "Server will be restarted after all running games are finished.");
        } else if (_hallExceptionMessage != null) {
            responseWriter.writeXmlMarshalExceptionResponse(_hallExceptionMessage);
        } else if (validateTableName(request.userName()) instanceof String errorMessage &&
                !errorMessage.isEmpty()) {
            responseWriter.writeXmlMarshalExceptionResponse(errorMessage);
        } else {
            User librarianUser = null;
            try {
                librarianUser = _adminService.getPlayer("librarian");
            } catch(UserNotFoundException ignored) {
                // If librarian user can't be found, don't use it
            }
            CardDeck cardDeck = _deckDAO.getDeckIfOwnedOrInLibrary(request.user(), librarianUser, _deckName);
            if (cardDeck == null) {
                responseWriter.writeXmlMarshalExceptionResponse(
                        "Unable to find deck '" + _deckName + "' in your deck list or the deck library");
            } else {
                try {
                    cardDeck = _hallServer.validateDeckIsLegal(_format, _cardBlueprintLibrary, cardDeck);
                } catch(HallException exp) {
                    responseWriter.writeXmlMarshalExceptionResponse(exp);
                }
                try {
                    GameSettings gameSettings = new GameSettings(_format, _league, _series, _league != null,
                            _isPrivate, _isInviteOnly, !_isVisible, _timer, _desc);
                    _hallServer.createNewTable(resourceOwner, gameSettings, cardDeck, _gameServer, _leagueService);
                    responseWriter.writeXmlOkResponse();
                } catch (Exception ex) {
                    //This is a worthless error that doesn't need to be spammed into the log
                    if (doNotIgnoreError(ex)) {
                        LOGGER.error("Error response for {}", request.uri(), ex);
                    }
                    responseWriter.writeXmlMarshalExceptionResponse(
                            "Failed to create table. Please try again later.");
                }
            }
        }
    }

    private String validateTableName(String requestingUserName) {
        String errorMessage = "";
        if (_isInviteOnly) {
            if (_desc.isEmpty()) {
                errorMessage = "Invite-only games must have your intended opponent in the description";
            } else if (_desc.equalsIgnoreCase(requestingUserName)) {
                errorMessage = "Absolutely no playing with yourself!!  Private matches must be with someone else.";
            } else {
                try {
                    User player = _adminService.getPlayer(_desc);
                    if (player == null) {
                        String expMessage = "Cannot find player '" + _desc + "'. " +
                                "Check your spelling and capitalization and ensure it is exact.";
                        throw new UserNotFoundException(expMessage);
                    }
                } catch (UserNotFoundException ex) {
                    errorMessage = "Cannot find player '" + _desc + "'. " +
                            "Check your spelling and capitalization and ensure it is exact.";
                }
            }
        }
        return errorMessage;
    }

    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

}