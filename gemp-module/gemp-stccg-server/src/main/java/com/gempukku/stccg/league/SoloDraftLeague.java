package com.gempukku.stccg.league;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.*;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.formats.FormatLibrary;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoloDraftLeague extends League {
    public static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;
    private final SoloDraft _draft;
    private final long _code;
    private final LeagueSeries _leagueSeries;

    public SoloDraftLeague(int cost, String name, int status, CollectionType collectionType,
                           CardBlueprintLibrary library, ZonedDateTime startTime, int seriesDuration,
                           int maxMatches, SoloDraft draft, long draftCode, FormatLibrary formatLibrary) {
        super(cost, name, status, collectionType, -999, new FixedLeaguePrizes(library));
        _draft = draft;
        _code = draftCode;

        _leagueSeries = new LeagueSeries(seriesDuration, maxMatches, startTime, formatLibrary.get(_draft.getFormat()),
                "Series 1");
    }

    public SoloDraft getSoloDraft() {
        return _draft;
    }

    private long getSeed(User player) {
        return _collectionType.getCode().hashCode() + (long) player.getId() * HIGH_ENOUGH_PRIME_NUMBER;
    }

    @Override
    public void joinLeague(CollectionsManager collectionsManager, User player) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        long seed = getSeed(player);

        CardCollection leagueProduct = _draft.initializeNewCollection(seed);

        for (GenericCardItem collectionItem : leagueProduct.getAll())
            startingCollection.addItem(collectionItem.getBlueprintId(), collectionItem.getCount());

        startingCollection.setExtraInformation(createExtraInformation(seed));
        collectionsManager.addPlayerCollection(false, "Sealed league product", player, _collectionType,
                startingCollection);
    }

    private Map<String, Object> createExtraInformation(long seed) {
        Map<String, Object> extraInformation = new HashMap<>();
        extraInformation.put("finished", false);
        extraInformation.put("stage", 0);
        extraInformation.put("seed", seed);
        extraInformation.put("draftPool", _draft.initializeDraftPool(seed, _code));
        return extraInformation;
    }

    @Override
    public List<LeagueSeries> getAllSeries() {
        return List.of(_leagueSeries);
    }

    @Override
    public boolean isLimited() {
        return true;
    }

    @Override
    public void process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings) {

        if (_status == 0) {
            if (ZonedDateTime.now(_clock).isAfter(getEnd().plusDays(1))) {

                for (PlayerStanding leagueStanding : leagueStandings) {
                    CardCollection leaguePrize =
                            _leaguePrizes.getPrizeForLeague(leagueStanding.getStanding(), _collectionType);
                    if (leaguePrize != null)
                        collectionsManager.addAllCardsInCollectionToPlayerMyCardsCollection(true,
                                "End of league prizes", leagueStanding.getPlayerName(), leaguePrize);
                }
                _status++;
            }
        }
    }

}