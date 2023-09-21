package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.decisions.AwaitingDecision;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface GameStateListener {
    void cardCreated(LotroPhysicalCard card);
    void cardCreated(LotroPhysicalCard card, boolean overridePlayerVisibility);

    void cardMoved(LotroPhysicalCard card);

    void cardsRemoved(String playerPerforming, Collection<LotroPhysicalCard> cards);

    void initializeBoard(List<String> playerIds, boolean discardIsPublic);

    void setPlayerPosition(String playerId, int i);

    void setPlayerDecked(String playerId, boolean bool);
    void setPlayerScore(String playerId, int points);

    void setTwilight(int twilightPool);

    void setTribbleSequence(String tribbleSequence);

    void setCurrentPlayerId(String playerId);

    void setCurrentPhase(String currentPhase);

    void addAssignment(LotroPhysicalCard fp, Set<LotroPhysicalCard> minions);

    void removeAssignment(LotroPhysicalCard fp);

    void startSkirmish(LotroPhysicalCard fp, Set<LotroPhysicalCard> minions);

    void addToSkirmish(LotroPhysicalCard card);

    void removeFromSkirmish(LotroPhysicalCard card);

    void finishSkirmish();

    void addTokens(LotroPhysicalCard card, Token token, int count);

    void removeTokens(LotroPhysicalCard card, Token token, int count);

    void sendMessage(String message);

    void setSite(LotroPhysicalCard card);

    void sendGameStats(GameStats gameStats);

    void cardAffectedByCard(String playerPerforming, LotroPhysicalCard card, Collection<LotroPhysicalCard> affectedCard);

    void eventPlayed(LotroPhysicalCard card);

    void cardActivated(String playerPerforming, LotroPhysicalCard card);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision);

    void sendWarning(String playerId, String warning);

    void endGame();
}
