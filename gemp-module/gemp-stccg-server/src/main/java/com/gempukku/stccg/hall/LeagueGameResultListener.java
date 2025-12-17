package com.gempukku.stccg.hall;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;

import java.util.Map;

public class LeagueGameResultListener implements GameResultListener {

    private final League _league;
    private final LeagueSeriesData _seriesData;
    private final LeagueService _leagueService;

    public LeagueGameResultListener(League league, LeagueSeriesData seriesData, LeagueService leagueService) {
        _league = league;
        _seriesData = seriesData;
        _leagueService = leagueService;
    }

    public LeagueGameResultListener(GameSettings gameSettings, ServerObjects serverObjects) {
        this(gameSettings.getLeague(), gameSettings.getSeriesData(), serverObjects.getLeagueService());
    }


    @Override
    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        _leagueService.reportLeagueGameResult(
                _league, _seriesData, winnerPlayerId, loserReasons.keySet().iterator().next());
    }

    @Override
    public void gameCancelled() {
        // Do nothing...
    }
}