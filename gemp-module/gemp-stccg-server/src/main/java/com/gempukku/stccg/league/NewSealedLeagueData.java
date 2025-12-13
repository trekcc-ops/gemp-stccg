package com.gempukku.stccg.league;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.*;

public class NewSealedLeagueData implements LeagueData {
    private final String _leagueTemplateName;
    private final List<LeagueSeriesData> _allSeries;
    private final CollectionType _collectionType;
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    private final FormatLibrary _formatLibrary;
    private final int _maxMatches;
    private final String _creationTime;
    private final String _collectionCode;

    public NewSealedLeagueData(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary, String leagueTemplateName,
                               int start, int seriesDuration, int maxMatches, String creationTime, String collectionCode) {
        _leaguePrizes = new FixedLeaguePrizes(cardLibrary);
        _formatLibrary = formatLibrary;
        _leagueTemplateName = leagueTemplateName;
        _maxMatches = maxMatches;
        _creationTime = creationTime;
        _collectionCode = collectionCode;
        _collectionType = new CollectionType(creationTime, collectionCode);

        var def = _formatLibrary.GetSealedTemplate(_leagueTemplateName);

        _allSeries = new LinkedList<>();
        for (int i = 0; i < def.GetSeriesCount(); i++) {
            _allSeries.add(
                    new DefaultLeagueSeriesData(_leaguePrizes, true, "Series " + (i + 1),
                            DateUtils.offsetDate(start, i * seriesDuration),
                            DateUtils.offsetDate(start, (i + 1) * seriesDuration - 1), maxMatches,
                            def.getFormat(), _collectionType));
        }
    }

    public NewSealedLeagueData(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary, String leagueName,
                               int start, int seriesDuration, int maxMatches, long creationTime,
                               String collectionName) {
        this(cardLibrary, formatLibrary, leagueName, start, seriesDuration, maxMatches, String.valueOf(creationTime),
            collectionName);
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
    public void joinLeague(CollectionsManager collectionManager, User player, int currentTime) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        for (int i = 0; i < _allSeries.size(); i++) {
            LeagueSeriesData series = _allSeries.get(i);
            if (currentTime >= series.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_leagueTemplateName);
                var leagueProduct = sealedLeague.GetProductForSeries(i);

                for (GenericCardItem collectionItem : leagueProduct)
                    startingCollection.addItem(collectionItem.getBlueprintId(), collectionItem.getCount());
            }
        }
        collectionManager.addPlayerCollection(
                true, "Sealed league product", player, _collectionType, startingCollection);
    }

    @Override
    public int process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings,
                       int oldStatus, int currentTime) {
        int status = oldStatus;

        for (int i = status; i < _allSeries.size(); i++) {
            LeagueSeriesData series = _allSeries.get(i);
            if (currentTime >= series.getStart()) {
                var sealedLeague = _formatLibrary.GetSealedTemplate(_leagueTemplateName);
                var leagueProduct = sealedLeague.GetProductForSeries(i);
                Map<User, CardCollection> map = collectionsManager.getPlayersCollection(_collectionType.getCode());
                for (Map.Entry<User, CardCollection> playerCardCollectionEntry : map.entrySet()) {
                    collectionsManager.addItemsToUserCollection(
                            true, "New sealed league product", playerCardCollectionEntry.getKey(),
                            _collectionType, leagueProduct);
                }
                status = i + 1;
            }
        }

        if (status == _allSeries.size()) {
            if (currentTime > DateUtils.offsetDate(_allSeries.getLast().getEnd(), 1)) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize =
                            _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerCollection(
                                true, "End of league prizes", leagueStanding.getPlayerName(),
                                _prizeCollectionType, leaguePrize.getAll()
                        );
                }
                status++;
            }
        }

        return status;
    }

    public String getName() {
        return _leagueTemplateName;
    }

    public String getCreationTime() {
        return _creationTime;
    }

    public String getSerializedParameters() {
        StringJoiner sj = new StringJoiner(",");
        sj.add(_collectionCode);
        sj.add(String.valueOf(getSeries().getFirst().getStart()));
        sj.add(String.valueOf(getSeries().getLast().getEnd()));
        sj.add(String.valueOf(_maxMatches));
        sj.add(_creationTime);
        sj.add(_leagueTemplateName);
        return sj.toString();
    }
}