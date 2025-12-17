package com.gempukku.stccg.hall;

import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;

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

    public GameSettings(String formatSelection, GameTimer timer, String description, boolean isInviteOnly,
                        boolean isPrivate, boolean isHidden, FormatLibrary formatLibrary, LeagueService leagueService)
            throws HallException {
        League league = null;
        LeagueSeriesData seriesData = null;
        GameFormat format = formatLibrary.getHallFormats().get(formatSelection);
        GameTimer gameTimer = timer;

        if (format == null) {
            // Maybe it's a league format?
            try {
                league = leagueService.getLeagueByType(formatSelection);
                seriesData = leagueService.getCurrentLeagueSeries(league);
                if (seriesData == null)
                    throw new HallException("There is no ongoing series for that league");

                if (isInviteOnly) {
                    throw new HallException("League games cannot be invite-only");
                }

                if (isPrivate) {
                    throw new HallException("League games cannot be private");
                }

                //Don't want people getting around the anonymity for leagues.
                if (description != null)
                    description = "";

                format = seriesData.getFormat();

                gameTimer = GameTimer.COMPETITIVE_TIMER;
            } catch(LeagueNotFoundException ignored) {

            }
        }
        // It's not a normal format and also not a league one
        if (format == null)
            throw new HallException("This format is not supported: " + formatSelection);

        this.gameFormat = format;
        this.league = league;
        this.seriesData = seriesData;
        this.competitive = league != null;
        this.privateGame = isPrivate;
        _gameIsVisible = isHidden;
        this.timeSettings = gameTimer;
        this.userDescription = description;
        this.isInviteOnly = isInviteOnly;
    }


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

    public GameType getGameType() { return gameFormat.getGameType(); }

    public String getTournamentNameForHall() {
        final League league = getLeague();
        final LeagueSeriesData seriesData = getSeriesData();

        return (league != null) ?
                (league.getName() + " - " + seriesData.getName()) :
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
}