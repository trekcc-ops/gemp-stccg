package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.collection.CollectionType;

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

    String getTournamentId();
    String getFormat();
    CollectionType getCollectionType();
    String getTournamentName();
    String getPlayOffSystem();

    Stage getTournamentStage();
    int getCurrentRound();
    int getPlayersInCompetitionCount();

    boolean advanceTournament(TournamentCallback tournamentCallback, CollectionsManager collectionsManager);

    void reportGameFinished(String winner, String loser);

    void dropPlayer(String player);

    List<PlayerStanding> getCurrentStandings();

    boolean isPlayerInCompetition(String player);
}