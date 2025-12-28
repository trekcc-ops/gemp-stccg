package com.gempukku.stccg.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.formats.GameFormat;

import java.util.List;

public interface Tournament {
    enum Stage {
        DECK_BUILDING("Deck building"),
        PLAYING_GAMES("Playing games"), FINISHED("Finished");

        private final String _humanReadable;

        Stage(String humanReadable) {
            _humanReadable = humanReadable;
        }

        public String getHumanReadable() {
            return _humanReadable;
        }
    }

    @JsonProperty("tournamentId")
    String getTournamentId();

    @JsonProperty("formatName")
    String getFormat();

    @JsonIgnore
    CollectionType getCollectionType();

    @JsonProperty("collectionName")
    default String getCollectionName() {
        return getCollectionType().getFullName();
    }
    @JsonProperty("tournamentName")
    String getTournamentName();

    @JsonIgnore
    String getPlayOffSystem();

    @JsonProperty("currentStage")
    Stage getTournamentStage();

    @JsonProperty("currentRound")
    int getCurrentRound();

    @JsonIgnore
    int getPlayersInCompetitionCount();

    boolean advanceTournament(TournamentCallback tournamentCallback, CollectionsManager collectionsManager);

    void reportGameFinished(String winner, String loser);

    void dropPlayer(String player);

    @JsonProperty("standings")
    List<PlayerStanding> getCurrentStandings();

    boolean isPlayerInCompetition(String player);

    GameFormat getGameFormat();
}