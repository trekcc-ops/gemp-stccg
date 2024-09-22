package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SealedLeagueData implements LeagueData {
    private final String _format;
    private final List<LeagueSeriesData> _allSeries;
    private final CollectionType _collectionType;
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    private final FormatLibrary _formatLibrary;

    public SealedLeagueData(CardBlueprintLibrary library, FormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(library);
        _formatLibrary = formatLibrary;
        
        String[] params = parameters.split(",");
        _format = params[0];
        int start = Integer.parseInt(params[1]);
        _collectionType = new CollectionType(params[2], params[3]);

        int seriesDuration = 7;
        int maxMatches = 10;

        _allSeries = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            _allSeries.add(
                    new DefaultLeagueSeriesData(_leaguePrizes, true, "Week " + (i + 1),
                            DateUtils.offsetDate(start, i * seriesDuration), DateUtils.offsetDate(start, (i + 1) * seriesDuration - 1), maxMatches,
                            formatLibrary.getFormatByName(_format), _collectionType));
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
        return Collections.unmodifiableList(_allSeries);
    }

    @Override
    public void joinLeague(CollectionsManager collectionManager, User player, int currentTime) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        for (int i = 0; i < _allSeries.size(); i++) {
            LeagueSeriesData seriesData = _allSeries.get(i);
            if (currentTime >= seriesData.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_format);
                var leagueProduct = sealedLeague.GetProductForSeries(i);

                for (GenericCardItem serieCollectionItem : leagueProduct)
                    startingCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());
            }
        }
        collectionManager.addPlayerCollection(true, "Sealed league product", player, _collectionType, startingCollection);
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, int currentTime) {
        int status = oldStatus;

        for (int i = status; i < _allSeries.size(); i++) {
            LeagueSeriesData seriesData = _allSeries.get(i);
            if (currentTime >= seriesData.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_format);
                var leagueProduct = sealedLeague.GetProductForSeries(i);

                Map<User, CardCollection> map = collectionsManager.getPlayersCollection(_collectionType.getCode());
                for (Map.Entry<User, CardCollection> playerCardCollectionEntry : map.entrySet()) {
                    collectionsManager.addItemsToPlayerCollection(true, "New sealed league product", playerCardCollectionEntry.getKey(), _collectionType, leagueProduct);
                }
                status = i + 1;
            }
        }

        if (status == _allSeries.size()) {
            int maxGamesPlayed = 0;
            for (LeagueSeriesData seriesData : _allSeries) {
                maxGamesPlayed+=seriesData.getMaxMatches();
            }

            LeagueSeriesData lastSeries = _allSeries.get(_allSeries.size() - 1);
            if (currentTime > DateUtils.offsetDate(lastSeries.getEnd(), 1)) {
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
}
