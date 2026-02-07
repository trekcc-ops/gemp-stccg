package com.gempukku.stccg.hall;

import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeries;
import com.gempukku.stccg.league.LeagueService;

import java.util.Map;

public class LeagueGameResultListener implements GameResultListener {

    private final League _league;
    private final LeagueSeries _series;
    private final LeagueService _leagueService;

    public LeagueGameResultListener(League league, LeagueSeries series, LeagueService leagueService) {
        _league = league;
        _series = series;
        _leagueService = leagueService;
    }


    public LeagueGameResultListener(GameSettings gameSettings, LeagueService leagueService) {
        this(gameSettings.getLeague(), gameSettings.getSeries(), leagueService);
    }


    @Override
    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        _leagueService.reportLeagueGameResult(
                _league, _series, winnerPlayerId, loserReasons.keySet().iterator().next());
    }

    @Override
    public void gameCancelled() {
        // Do nothing...
    }
}