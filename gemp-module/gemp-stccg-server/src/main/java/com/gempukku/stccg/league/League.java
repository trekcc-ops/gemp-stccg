package com.gempukku.stccg.league;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

@JsonIgnoreProperties(value = { "start", "end" }, allowGetters = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "class")
public abstract class League implements Iterable<LeagueSeries> {
    protected final Clock _clock;
    private final int _cost;
    private final String _name;
    private final int _leagueId;
    int _status;
    protected final CollectionType _collectionType;
    protected final LeaguePrizes _leaguePrizes;

    protected League(int cost, String name, int status, CollectionType collectionType, int leagueId,
                  LeaguePrizes leaguePrizes, Clock clock) {
        _leaguePrizes = leaguePrizes;
        _name = name;
        _cost = cost;
        _collectionType = collectionType;
        _status = status;
        _leagueId = leagueId;
        _clock = clock;
    }


    protected League(int cost, String name, int status, CollectionType collectionType, int leagueId,
                  LeaguePrizes leaguePrizes) {
        this(cost, name, status, collectionType, leagueId, leaguePrizes, Clock.systemUTC());
    }

    @JsonProperty("cost")
    public int getCost() {
        return _cost;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    @JsonProperty("leagueId")
    public int getLeagueId() {
        return _leagueId;
    }

    @JsonProperty("status")
    public int getStatus() {
        return _status;
    }

    @JsonProperty("allSeries")
    public abstract List<LeagueSeries> getAllSeries();
    @JsonProperty("start")
    public ZonedDateTime getStart() {
        return getAllSeries().getFirst().getStart();
    }

    @JsonProperty("end")
    public ZonedDateTime getEnd() {
        return getAllSeries().getLast().getEnd();
    }

    @JsonProperty("collectionType")
    public CollectionType getCollectionType() {
        return _collectionType;
    }

    @JsonIgnore
    public abstract boolean isLimited();

    @NotNull
    @Override
    public Iterator<LeagueSeries> iterator() {
        return getAllSeries().iterator();
    }

    public abstract void process(CollectionsManager collectionsManager, List<? extends PlayerStanding> leagueStandings);

    public abstract void joinLeague(CollectionsManager collectionsManager, User player);

    public CardCollection getPrizeForLeagueMatchWinner(int winCountThisSeries) {
        return _leaguePrizes.getPrizeForLeagueMatchWinner(winCountThisSeries);
    }

    @JsonIgnore
    public int getMaxRepeatMatchesPerSeries() {
        return 1;
    }

    public void setStatus(int newStatus) {
        _status = newStatus;
    }

}