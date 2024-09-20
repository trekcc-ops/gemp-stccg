package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;

import java.util.Collection;
import java.util.List;

public interface GameStateListener {
    String getPlayerId();
    void cardCreated(PhysicalCard card, GameEvent.Type eventType);

    void putCardIntoPlay(PhysicalCard card, boolean restoreSnapshot);
    void cardCreated(PhysicalCard card, boolean overridePlayerVisibility);

    void cardMoved(PhysicalCard card);

    void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards);

    void initializeBoard(List<String> playerIds, boolean discardIsPublic);

    void setPlayerDecked(String playerId, boolean bool);
    void setPlayerScore(String playerId, int points);

    void setTribbleSequence(String tribbleSequence);

    void setCurrentPlayerId(String playerId);

    void setCurrentPhase(String currentPhase);

    void sendMessage(String message);

    void sendGameStats(GameStats gameStats);

    void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard);

    void eventPlayed(PhysicalCard card);

    void cardActivated(String playerPerforming, PhysicalCard card);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision);

    void sendWarning(String playerId, String warning);

    void endGame();

    void cardImageUpdated(PhysicalCard card);
}
