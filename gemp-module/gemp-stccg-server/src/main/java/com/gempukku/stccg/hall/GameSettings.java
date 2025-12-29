package com.gempukku.stccg.hall;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeries;

public class GameSettings {
    private final GameFormat gameFormat;
    private final League league;
    private final LeagueSeries series;
    private final boolean competitive;
    private final boolean privateGame;
    private final boolean _gameIsVisible;
    private final GameTimer timeSettings;
    private final String userDescription;
    private final boolean isInviteOnly;

    public GameSettings(GameFormat gameFormat, League league, LeagueSeries series,
                        boolean competitive, boolean privateGame, boolean isInviteOnly, boolean hiddenGame,
                        GameTimer timer, String description) {
        this.gameFormat = gameFormat;
        this.league = league;
        this.series = series;
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

    public final LeagueSeries getSeries() {
        return series;
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

    public GameType getGameType() { return gameFormat.getGameType(); }

    public String getTournamentNameForHall() {
        final League league = getLeague();
        final LeagueSeries series = getSeries();

        return (league != null) ?
                (league.getName() + " - " + series.getName()) :
                ("Casual - " + getTimeSettings().name());
    }
    public boolean allowsSpectators() {
        return (getLeague() != null) || (!isCompetitive() && !isPrivateGame() && !isHiddenGame());
    }

    public String getGameTypeName() {
        return gameFormat.getGameType().name();
    }

    public String getFormatName() {
        return gameFormat.getName();
    }

    public CardGameMediator createGameMediator(GameParticipant[] participants,
                                               CardBlueprintLibrary cardBlueprintLibrary, String gameName) {
        return new CardGameMediator(participants, cardBlueprintLibrary, allowsSpectators(), getTimeSettings(),
                getGameFormat(), getGameType(), isCompetitive(), gameName);

    }
}