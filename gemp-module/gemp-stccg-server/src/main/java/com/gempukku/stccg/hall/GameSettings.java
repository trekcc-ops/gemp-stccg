package com.gempukku.stccg.hall;

import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;

public class GameSettings {
    private final GameFormat gameFormat;
    private final League league;
    private final LeagueSeriesData seriesData;
    private final boolean competitive;
    private final boolean privateGame;
    private final boolean _gameIsVisible;
    private final GameTimer timeSettings;
    private final String userDescription;
    private final boolean isInviteOnly;

    public GameSettings(GameFormat gameFormat, League league, LeagueSeriesData seriesData,
                        boolean competitive, boolean privateGame, boolean isInviteOnly, boolean hiddenGame,
                        GameTimer timer, String description) {
        this.gameFormat = gameFormat;
        this.league = league;
        this.seriesData = seriesData;
        this.competitive = competitive;
        this.privateGame = privateGame;
        _gameIsVisible = !hiddenGame;
        this.timeSettings = timer;
        this.userDescription = description;
        this.isInviteOnly = isInviteOnly;
    }

    public final GameFormat getGameFormat() {
        return gameFormat;
    }

    public final League getLeague() {
        return league;
    }

    public final LeagueSeriesData getSeriesData() {
        return seriesData;
    }

    public final boolean isCompetitive() {
        return competitive;
    }

    public final boolean isPrivateGame() {
        return privateGame;
    }

    public final boolean isHiddenGame() {
        return !_gameIsVisible;
    }

    public final GameTimer getTimeSettings() { return timeSettings; }

    public final String getUserDescription() { return userDescription; }

    public final boolean isUserInviteOnly() { return isInviteOnly; }
}