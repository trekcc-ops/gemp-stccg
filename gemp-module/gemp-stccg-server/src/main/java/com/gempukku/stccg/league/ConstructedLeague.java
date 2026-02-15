package com.gempukku.stccg.league;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConstructedLeague extends League {

    private final List<LeagueSeries> _allSeries;

    @JsonCreator
    private ConstructedLeague(@JsonProperty(value = "cost", required = true)
                             int cost,
                             @JsonProperty(value = "name", required = true)
                             String name,
                             @JsonProperty(value = "collectionType", required = true)
                             CollectionType collectionType,
                             @JsonProperty(value = "allSeries", required = true)
                             List<LeagueSeries> allSeries,
                             @JsonProperty(value = "status", required = true)
                             int status,
                             @JsonProperty(value = "leagueId", required = true)
                             int leagueId,
                             @JacksonInject CardBlueprintLibrary cardLibrary
    ) {
        super(cost, name, status, collectionType, leagueId, new FixedLeaguePrizes(cardLibrary));
        _allSeries = allSeries;
    }

    public ConstructedLeague(int cost, String name, CollectionType collectionType, FormatLibrary formatLibrary,
                             CardBlueprintLibrary cardLibrary, List<Integer> durations, List<Integer> maxMatches,
                             List<String> formats, ZonedDateTime startTime) {
        super(cost, name, 0, collectionType, -999, new FixedLeaguePrizes(cardLibrary));
        _allSeries = new ArrayList<>();
        ZonedDateTime seriesStartTime = startTime;
        for (int i = 0; i < formats.size(); i++) {
            ZonedDateTime seriesEndTime = seriesStartTime.plusDays(durations.get(i));
            _allSeries.add(new LeagueSeries(maxMatches.get(i), seriesStartTime,
                    seriesEndTime, formats.get(i), name, formatLibrary));
            seriesStartTime = seriesEndTime;
        }
    }


    public ConstructedLeague(int cost, String name, CollectionType collectionType, List<LeagueSeries> allSeries,
                             CardBlueprintLibrary cardLibrary) {
        this(cost, name, collectionType, allSeries, 0, -999, cardLibrary);
    }

    @Override
    public List<LeagueSeries> getAllSeries() {
        return _allSeries;
    }

    public ZonedDateTime getStart() {
        return _allSeries.getFirst().getStart();
    }

    public ZonedDateTime getEnd() {
        return _allSeries.getLast().getEnd();
    }

    public boolean isLimited() {
        return false;
    }

    @Override
    public void process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings) {
        if (_status == 0) {
            if (getEnd().plusDays(1).isAfter(ZonedDateTime.now())) {
                for (PlayerStanding leagueStanding : leagueStandings) {
                    int standing = leagueStanding.getStanding();
                    CardCollection leaguePrize = _leaguePrizes.getPrizeForLeague(standing, _collectionType);
                    if (leaguePrize != null) {
                        String playerName = leagueStanding.getPlayerName();
                        Iterable<GenericCardItem> prizes = leaguePrize.getAll();
                        collectionsManager.addItemsToPlayerMyCardsCollection(
                                true, "End of league prizes", playerName, prizes);
                    }
                }
                _status++;
            }
        }
    }

    @Override
    public void joinLeague(CollectionsManager collectionsManager, User player) {
    }



}