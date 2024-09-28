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

public class NewSealedLeagueData implements LeagueData {
    private final String _leagueTemplateName;
    private final List<LeagueSeriesData> _series;
    private final CollectionType _collectionType;
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    //private final SealedLeagueProduct _leagueProduct;
    private final FormatLibrary _formatLibrary;

    public NewSealedLeagueData(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(cardLibrary);
        _formatLibrary = formatLibrary;
        
        String[] params = parameters.split(",");
        _leagueTemplateName = params[0];
        int start = Integer.parseInt(params[1]);
        int seriesDuration = Integer.parseInt(params[2]);
        int maxMatches = Integer.parseInt(params[3]);

        _collectionType = new CollectionType(params[4], params[5]);

        var def = _formatLibrary.GetSealedTemplate(_leagueTemplateName);

        _series = new LinkedList<>();
        for (int i = 0; i < def.GetSeriesCount(); i++) {
            _series.add(
                    new DefaultLeagueSeriesData(_leaguePrizes, true, "Serie " + (i + 1),
                            DateUtils.offsetDate(start, i * seriesDuration), DateUtils.offsetDate(start, (i + 1) * seriesDuration - 1), maxMatches,
                            def.GetFormat(), _collectionType));
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
    public void joinLeague(CollectionsManager collectionManager, User player, int currentTime) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        for (int i = 0; i < _series.size(); i++) {
            LeagueSeriesData serie = _series.get(i);
            if (currentTime >= serie.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_leagueTemplateName);
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

        for (int i = status; i < _series.size(); i++) {
            LeagueSeriesData serie = _series.get(i);
            if (currentTime >= serie.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_leagueTemplateName);
                var leagueProduct = sealedLeague.GetProductForSeries(i);
                Map<User, CardCollection> map = collectionsManager.getPlayersCollection(_collectionType.getCode());
                for (Map.Entry<User, CardCollection> playerCardCollectionEntry : map.entrySet()) {
                    collectionsManager.addItemsToPlayerCollection(true, "New sealed league product", playerCardCollectionEntry.getKey(), _collectionType, leagueProduct);
                }
                status = i + 1;
            }
        }

        int maxGamesTotal = 0;
        for (LeagueSeriesData sery : _series)
            maxGamesTotal+=sery.getMaxMatches();

        if (status == _series.size()) {
            LeagueSeriesData lastSerie = _series.getLast();
            if (currentTime > DateUtils.offsetDate(lastSerie.getEnd(), 1)) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), leagueStandings.size(), leagueStanding.getGamesPlayed(), maxGamesTotal, _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league prizes", leagueStanding.getPlayerName(), _prizeCollectionType, leaguePrize.getAll());
                    final CardCollection leagueTrophies = _leaguePrizes.getTrophiesForLeague(leagueStanding.getStanding(), leagueStandings.size(), leagueStanding.getGamesPlayed(), maxGamesTotal, _collectionType);
                    if (leagueTrophies != null)
                        collectionsManager.addItemsToPlayerCollection(true, "End of league trophies", leagueStanding.getPlayerName(), CollectionType.TROPHY, leagueTrophies.getAll());
                }
                status++;
            }
        }

        return status;
    }
}