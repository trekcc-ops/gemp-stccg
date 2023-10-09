package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.game.User;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewConstructedLeagueData implements LeagueData {
    private final LeaguePrizes _leaguePrizes;
    private final List<LeagueSeriesData> _series = new ArrayList<>();

    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final CollectionType _collectionType;

    private final int _maxRepeatGames;

    public NewConstructedLeagueData(CardBlueprintLibrary library, FormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(library);
        
        String[] params = parameters.split(",");
        int start = Integer.parseInt(params[0]);

        _collectionType = CollectionType.getCollectionTypeByCode(params[1]);
        if (_collectionType == null)
            throw new IllegalArgumentException("Unknown collection type");

        _maxRepeatGames = Integer.parseInt(params[3]);
        int series = Integer.parseInt(params[4]);

        int serieStart = start;
        for (int i = 0; i < series; i++) {
            String format = params[5 + i * 3];
            int duration = Integer.parseInt(params[6 + i * 3]);
            int maxMatches = Integer.parseInt(params[7 + i * 3]);
            _series.add(new DefaultLeagueSeriesData(_leaguePrizes, false, "Serie " + (i + 1),
                    serieStart, DateUtils.offsetDate(serieStart, duration - 1),
                    maxMatches, formatLibrary.getFormat(format), _collectionType));

            serieStart = DateUtils.offsetDate(serieStart, duration);
        }
    }

    @Override
    public boolean isSoloDraftLeague() {
        return false;
    }

    @Override
    public SoloDraft getSoloDraft() {
        return null;
    }

    @Override
    public List<LeagueSeriesData> getSeries() {
        return Collections.unmodifiableList(_series);
    }

    @Override
    public void joinLeague(CollectionsManager collecionsManager, User player, int currentTime) {
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, int currentTime) {
        int status = oldStatus;
        if (status == 0) {
            int maxGamesPlayed = 0;
            for (LeagueSeriesData sery : _series) {
                maxGamesPlayed+=sery.getMaxMatches();
            }

            LeagueSeriesData lastSerie = _series.get(_series.size() - 1);
            if (currentTime > DateUtils.offsetDate(lastSerie.getEnd(), 1)) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), leagueStandings.size(), leagueStanding.getGamesPlayed(), maxGamesPlayed, _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.getPlayerName(), _prizeCollectionType, leaguePrize.getAll());
                    final CardCollection leagueTrophies = _leaguePrizes.getTrophiesForLeague(leagueStanding.getStanding(), leagueStandings.size(), leagueStanding.getGamesPlayed(), maxGamesPlayed, _collectionType);
                    if (leagueTrophies != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league trophies", leagueStanding.getPlayerName(), CollectionType.TROPHY, leagueTrophies.getAll());
                }
                status++;
            }
        }

        return status;
    }

    @Override
    public int getMaxRepeatMatchesPerSerie() {
        return _maxRepeatGames;
    }
}
