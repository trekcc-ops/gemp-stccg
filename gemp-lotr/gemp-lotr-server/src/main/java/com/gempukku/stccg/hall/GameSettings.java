package com.gempukku.stccg.hall;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.game.GameFormat;
import com.gempukku.stccg.league.LeagueSeriesData;

public class GameSettings {
    private final CollectionType collectionType;
    private final GameFormat gameFormat;
    private final League league;
    private final LeagueSeriesData seriesData;
    private final boolean competitive;
    private final boolean privateGame;
    private final boolean hiddenGame;
    private final GameTimer timeSettings;
    private final String userDescription;
    private final boolean isInviteOnly;

    public GameSettings(CollectionType collectionType, GameFormat gameFormat, League league, LeagueSeriesData seriesData,
                        boolean competitive, boolean privateGame, boolean isInviteOnly, boolean hiddenGame,
                        GameTimer timer, String description) {
        this.collectionType = collectionType;
        this.gameFormat = gameFormat;
        this.league = league;
        this.seriesData = seriesData;
        this.competitive = competitive;
        this.privateGame = privateGame;
        this.hiddenGame = hiddenGame;
        this.timeSettings = timer;
        this.userDescription = description;
        this.isInviteOnly = isInviteOnly;
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }

    public GameFormat getLotroFormat() {
        return gameFormat;
    }

    public League getLeague() {
        return league;
    }

    public LeagueSeriesData getSeriesData() {
        return seriesData;
    }

    public boolean isCompetitive() {
        return competitive;
    }

    public boolean isPrivateGame() {
        return privateGame;
    }

    public boolean isHiddenGame() {
        return hiddenGame;
    }

    public GameTimer getTimeSettings() { return timeSettings; }

    public String getUserDescription() { return userDescription; }

    public boolean isUserInviteOnly() { return isInviteOnly; }
}
