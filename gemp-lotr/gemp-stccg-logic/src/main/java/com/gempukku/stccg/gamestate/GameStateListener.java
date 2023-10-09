package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Token;
import com.gempukku.stccg.decisions.AwaitingDecision;

import java.util.Collection;
import java.util.List;

public interface GameStateListener {
    void cardCreated(PhysicalCard card, GameEvent.Type eventType);
    void putCardIntoPlay(PhysicalCard card);
    void cardCreated(PhysicalCard card, boolean overridePlayerVisibility);

    void cardMoved(PhysicalCard card);

    void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards);

    void initializeBoard(List<String> playerIds, boolean discardIsPublic);

    void setPlayerPosition(String playerId, int i);

    void setPlayerDecked(String playerId, boolean bool);
    void setPlayerScore(String playerId, int points);

    void setTwilight(int twilightPool);

    void setTribbleSequence(String tribbleSequence);

    void setCurrentPlayerId(String playerId);

    void setCurrentPhase(String currentPhase);

    void addTokens(PhysicalCard card, Token token, int count);

    void sendMessage(String message);

    void setSite(PhysicalCard card);

    void sendGameStats(GameStats gameStats);

    void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard);

    void eventPlayed(PhysicalCard card);

    void cardActivated(String playerPerforming, PhysicalCard card);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision);

    void sendWarning(String playerId, String warning);

    void endGame();
}
