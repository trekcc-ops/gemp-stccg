package com.gempukku.lotro.tournament;

import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;

import java.util.List;

public interface Tournament {
    enum Stage {
        DRAFT("Drafting"), DECK_BUILDING("Deck building"), PLAYING_GAMES("Playing games"), FINISHED("Finished");

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

    void playerChosenCard(String playerName, String cardId);
    void playerSummittedDeck(String player, CardDeck deck);
    CardDeck getPlayerDeck(String player);
    void dropPlayer(String player);

    Draft getDraft();

    List<PlayerStanding> getCurrentStandings();

    boolean isPlayerInCompetition(String player);
}
