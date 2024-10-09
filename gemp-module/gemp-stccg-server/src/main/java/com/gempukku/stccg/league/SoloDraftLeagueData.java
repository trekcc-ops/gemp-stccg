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
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoloDraftLeagueData implements LeagueData {
    public static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;
    private final SoloDraft _draft;
    private final CollectionType _collectionType;
    private final long _code;
    private final CollectionType _prizeCollectionType = CollectionType.MY_CARDS;
    private final LeaguePrizes _leaguePrizes;
    private final LeagueSeriesData _serie;

    public SoloDraftLeagueData(CardBlueprintLibrary library, FormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions, String parameters) {
        _leaguePrizes = new FixedLeaguePrizes(library);

        String[] params = parameters.split(",");
        _draft = soloDraftDefinitions.getSoloDraft(params[0]);
        int start = Integer.parseInt(params[1]);
        int seriesDuration = Integer.parseInt(params[2]);
        int maxMatches = Integer.parseInt(params[3]);
        _code = Long.parseLong(params[4]);

        _collectionType = new CollectionType(params[4], params[5]);

        _serie = new DefaultLeagueSeriesData(_leaguePrizes, true, "Serie 1",
                DateUtils.offsetDate(start, 0), DateUtils.offsetDate(start, seriesDuration - 1), maxMatches,
                formatLibrary.getFormat(_draft.getFormat()), _collectionType);
    }

    public CollectionType getCollectionType() {
        return _collectionType;
    }

    public SoloDraft getSoloDraft() {
        return _draft;
    }

    @Override
    public boolean isSoloDraftLeague() {
        return true;
    }

    @Override
    public List<LeagueSeriesData> getSeries() {
        return Collections.singletonList(_serie);
    }

    private long getSeed(User player) {
        return _collectionType.getCode().hashCode() + (long) player.getId() * HIGH_ENOUGH_PRIME_NUMBER;
    }

    @Override
    public void joinLeague(CollectionsManager collectionsManager, User player, int currentTime) {
        MutableCardCollection startingCollection = new DefaultCardCollection();
        long seed = getSeed(player);

        CardCollection leagueProduct = _draft.initializeNewCollection(seed);

        for (GenericCardItem serieCollectionItem : leagueProduct.getAll())
            startingCollection.addItem(serieCollectionItem.getBlueprintId(), serieCollectionItem.getCount());

        startingCollection.setExtraInformation(createExtraInformation(seed));
        collectionsManager.addPlayerCollection(false, "Sealed league product", player, _collectionType, startingCollection);
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
    public int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, int currentTime) {
        int status = oldStatus;

        if (status == 0) {
            if (currentTime > DateUtils.offsetDate(_serie.getEnd(), 1)) {
                int maxGamesTotal = _serie.getMaxMatches();

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