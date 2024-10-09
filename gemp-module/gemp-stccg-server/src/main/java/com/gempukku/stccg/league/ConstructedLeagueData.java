package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.ArrayList;
import java.util.List;

public class ConstructedLeagueData implements LeagueData {
    private final LeaguePrizes _leaguePrizes;
    private final List<LeagueSeriesData> _allSeries = new ArrayList<>();
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final CollectionType _collectionType;

    // Example params - 20120312,fotr_block,0.7,default,All cards,7,10,3,fotr1_block,fotr_block,fotr2_block,fotr_block,fotr_block,fotr_block
    // Which means - start date,league prize pool,prizes multiplier,collection type,collection name,serie length,serie match count,series count,
    // serie1 format, serie1 prize pool,
    // serie2 format, serie2 prize pool,
    // serie3 format, serie3 prize pool,
    public ConstructedLeagueData(CardBlueprintLibrary library, FormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(library);
        
        String[] params = parameters.split(",");
        final int start = Integer.parseInt(params[0]);
        _collectionType = new CollectionType(params[3], params[4]);
        int days = Integer.parseInt(params[5]);
        int matchCount = Integer.parseInt(params[6]);
        int series = Integer.parseInt(params[7]);
        for (int i = 0; i < series; i++) {
            GameFormat format = formatLibrary.getFormat(params[8 + i * 2]);

            DefaultLeagueSeriesData data = new DefaultLeagueSeriesData(_leaguePrizes, false, "Week " + (i + 1),
                    DateUtils.offsetDate(start, i * days), DateUtils.offsetDate(start, ((i + 1) * days) - 1),
                    matchCount, format, _collectionType);
            _allSeries.add(data);
        }
    }

    @Override
    public boolean isSoloDraftLeague() {
        return false;
    }

    @Override
    public List<LeagueSeriesData> getSeries() {
        return _allSeries;
    }

    @Override
    public void joinLeague(CollectionsManager collectionsManager, User player, int currentTime) {
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus,
                       int currentTime) {
        int status = oldStatus;
        if (status == 0) {
            LeagueSeriesData lastSeries = _allSeries.getLast();
            if (currentTime > DateUtils.offsetDate(lastSeries.getEnd(), 1)) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.getPlayerName(), _prizeCollectionType, leaguePrize.getAll());
                }
                status++;
            }
        }

        return status;
    }
}