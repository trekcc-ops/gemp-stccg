package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewConstructedLeagueData implements LeagueData {
    private final LeaguePrizes _leaguePrizes;
    private final List<LeagueSeriesData> _allSeries = new ArrayList<>();

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

        for (int i = 0; i < series; i++) {
            int duration = Integer.parseInt(params[6 + i * 3]);
            int maxMatches = Integer.parseInt(params[7 + i * 3]);
            int endDate = DateUtils.offsetDate(start, duration - 1);
            GameFormat format = formatLibrary.get(params[5 + i * 3]);
            _allSeries.add(new DefaultLeagueSeriesData(_leaguePrizes, false, "Series " + (i + 1),
                    start, endDate, maxMatches, format, _collectionType));


            start = DateUtils.offsetDate(start, duration);
        }
    }

    @Override
    public boolean isSoloDraftLeague() {
        return false;
    }

    @Override
    public List<LeagueSeriesData> getSeries() {
        return Collections.unmodifiableList(_allSeries);
    }

    @Override
    public void joinLeague(CollectionsManager collectionsManager, User player, int currentTime) {
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings,
                       int oldStatus, int currentTime) {
        int status = oldStatus;
        if (status == 0) {
            LeagueSeriesData lastSeries = _allSeries.getLast();
            if (currentTime > DateUtils.offsetDate(lastSeries.getEnd(), 1)) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize =
                            _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), _collectionType);
                    if (leaguePrize != null) {
                        String playerName = leagueStanding.getPlayerName();
                        Iterable<GenericCardItem> prizes = leaguePrize.getAll();
                        collectionsManager.addItemsToPlayerCollection(
                                true, "End of league prizes", playerName, _prizeCollectionType, prizes);
                    }
                }
                status++;
            }
        }

        return status;
    }

    @Override
    public int getMaxRepeatMatchesPerSeries() {
        return _maxRepeatGames;
    }
}