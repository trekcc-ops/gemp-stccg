package com.gempukku.stccg.league;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.*;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.formats.SealedEventDefinition;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SealedLeague extends League {
    private final List<LeagueSeries> _allSeries;
    private final CardBlueprintLibrary _cardLibrary;
    private final SealedEventDefinition _eventDefinition;

    public SealedLeague(String format, CollectionType collectionType, CardBlueprintLibrary library,
                        ZonedDateTime startDate, FormatLibrary formatLibrary, int cost, String name,
                        int status, int seriesDuration, int maxMatches, int leagueId, int seriesCount,
                        Clock clock) {
        super(cost, name, status, collectionType, leagueId, new FixedLeaguePrizes(library), clock);
        _cardLibrary = library;
        _eventDefinition = formatLibrary.GetSealedTemplate(format);

        GameFormat gameFormat = formatLibrary.getFormatByName(format);

        _allSeries = new LinkedList<>();
        for (int i = 0; i < seriesCount; i++) {
            _allSeries.add(
                    new LeagueSeries(seriesDuration, maxMatches, startDate.plusDays((long) seriesDuration * i),
                            gameFormat, "Week " + (i + 1))
            );
        }
    }

    public SealedLeague(String format, CollectionType collectionType, CardBlueprintLibrary library,
                        ZonedDateTime startDate, FormatLibrary formatLibrary, int cost, String name,
                        int status, int seriesDuration, int maxMatches, int leagueId, int seriesCount) {
        this(format, collectionType, library, startDate, formatLibrary, cost, name, status, seriesDuration,
                maxMatches, leagueId, seriesCount, Clock.systemUTC());
    }


    @Override
    public void joinLeague(CollectionsManager collectionManager, User player) {
        ZonedDateTime now = ZonedDateTime.now(_clock);
        MutableCardCollection startingCollection = new DefaultCardCollection();
        for (int i = 0; i < _allSeries.size(); i++) {
            LeagueSeries series = _allSeries.get(i);
            if (now.isAfter(series.getStart())) {
                var leagueProduct = _eventDefinition.GetProductForSeries(i);
                for (GenericCardItem collectionItem : leagueProduct)
                    startingCollection.addItem(collectionItem.getBlueprintId(), collectionItem.getCount());
            }
        }
        collectionManager.addPlayerCollection(true, "Sealed league product", player, _collectionType,
                startingCollection);
    }

    @Override
    public List<LeagueSeries> getAllSeries() {
        return _allSeries;
    }

    @Override
    public boolean isLimited() {
        return true;
    }

    @Override
    public void process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings) {
        ZonedDateTime now = ZonedDateTime.now(_clock);

        int statusToIncrement = _status;

        for (int i = statusToIncrement; i < _allSeries.size(); i++) {
            LeagueSeries seriesData = _allSeries.get(i);
            if (now.isAfter(seriesData.getStart())) {
                List<GenericCardItem> leagueProduct = _eventDefinition.GetProductForSeries(i);

                Map<User, CardCollection> map = collectionsManager.getPlayersCollection(_collectionType.getCode());
                for (Map.Entry<User, CardCollection> playerCardCollectionEntry : map.entrySet()) {
                    User user = playerCardCollectionEntry.getKey();
                    collectionsManager.addItemsToUserCollection(true, "New sealed league product",
                            user, _collectionType, leagueProduct, _cardLibrary);
                }
                statusToIncrement = i + 1;
            }
        }

        if (statusToIncrement == _allSeries.size()) {
            if (now.isAfter(getEnd().plusDays(1))) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize =
                            _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addItemsToPlayerMyCardsCollection(
                                true, "End of league prizes", leagueStanding.getPlayerName(),
                                leaguePrize.getAll()
                        );
                }
                statusToIncrement++;
            }
        }

        _status = statusToIncrement;
    }

}