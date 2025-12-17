package com.gempukku.stccg.hall;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class GameTableView {
    private final User _user;
    private final GameTable _table;
    private final GameSettings _settings;
    private final CardGameMediator _mediator;
    public GameTableView(GameTable table, User user) {
        _user = user;
        _table = table;
        _settings = table.getGameSettings();
        _mediator = table.getMediator();
    }

    @JsonProperty("gameId")
    private String getGameId() {
        return _table.getGameId();
    }

    @JsonProperty("watchable")
    private String isWatchable() {
        return String.valueOf(_table.isWatchableToUser(_user));
    }

    @JsonProperty("status")
    private GameTable.TableStatus getStatus() {
        return _table.getStatus();
    }

    @JsonProperty("statusDescription")
    private String getStatusDescription() {
        return (_table.getStatus() == GameTable.TableStatus.WAITING || _mediator == null) ?
                "Waiting" : _mediator.getStatus();
    }

    @JsonProperty("gameType")
    private String getGameType() {
        return _settings.getGameTypeName();
    }

    @JsonProperty("format")
    private String getFormat() {
        return _settings.getFormatName();
    }

    @JsonProperty("userDescription")
    private String getUserDescription() {
        return _settings.getUserDescription();
    }

    @JsonProperty("isPrivate")
    private String isPrivate() {
        return String.valueOf(_settings.isPrivateGame());
    }

    @JsonProperty("isInviteOnly")
    private String isInviteOnly() {
        return String.valueOf(_settings.isUserInviteOnly());
    }

    @JsonProperty("tournament")
    private String getTournamentName() {
        return _settings.getTournamentNameForHall();
    }

    @JsonProperty("players")
    private String players() {
        return StringUtils.join(_table.getPlayerNames(), ",");
    }
    @JsonProperty("playing")
    private String isPlaying() {
        return String.valueOf(_table.hasPlayer(_user));
    }

    @JsonProperty("winner")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getWinner() {
        return (_mediator == null) ? null : _mediator.getWinner();
    }

}