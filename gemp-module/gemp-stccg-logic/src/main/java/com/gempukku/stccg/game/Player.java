package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "playerId", "score", "turnNumber", "cardsInZones", "decked" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class Player {
    private final String _playerId;
    private boolean _decked;
    private final Collection<Affiliation> _playedAffiliations = EnumSet.noneOf(Affiliation.class);
    private final DefaultGame _game;
    private int _currentScore;
    private int _lastSyncedScore;
    private int _turnNumber;

    public Player(DefaultGame game, String playerId) {
        _playerId = playerId;
        _decked = false;
        _game = game;
    }

    public DefaultGame getGame() { return _game; }

    public String getPlayerId() {
        return _playerId;
    }

    public boolean getDecked() {
        return _decked;
    }
    public void setDecked(boolean decked) {
        _decked = decked;
    }

    public void scorePoints(int points) {
        _currentScore = _currentScore + points;
    }

    public boolean isPlayingAffiliation(Affiliation affiliation) {
        return _playedAffiliations.contains(affiliation);
    }

    public void addPlayedAffiliation(Affiliation affiliation) {
        _playedAffiliations.add(affiliation);
    }

    public boolean hasCardInZone(Zone zone, int count, Filterable... cardFilter) {
        if (zone == Zone.HAND)
            return Filters.filter(_game.getGameState().getHand(_playerId), _game, cardFilter).size() >= count;
        else if (zone == Zone.DISCARD)
            return Filters.filter(_game.getGameState().getDiscard(_playerId), _game, cardFilter).size() >= count;
        else
            return false;
    }

    public boolean canDiscardFromHand(int count, Filterable... cardFilter) {
        return hasCardInZone(Zone.HAND, count, cardFilter);
    }

    public boolean hasACopyOfCardInPlay(PhysicalCard card) {
        for (PhysicalCard cardInPlay : _game.getGameState().getAllCardsInPlay()) {
            if (cardInPlay.isCopyOf(card) && cardInPlay.getOwner() == this)
                return true;
        }
        return false;
    }

    public boolean canLookOrRevealCardsInHandOfPlayer(String targetPlayerId) {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(targetPlayerId, _playerId);
    }

    public int getScore() {
        return _currentScore;
    }

    public Collection<PhysicalCard> getHand() {
        return _game.getGameState().getHand(_playerId);
    }

    public Collection<PhysicalCard> getDrawDeck() {
        return _game.getGameState().getDrawDeck(_playerId);
    }

    public Collection<PhysicalCard> getDiscardPile() {
        return _game.getGameState().getDiscard(_playerId);
    }

    public Collection<PhysicalCard> getRemovedPile() {
        return _game.getGameState().getRemoved(_playerId);
    }

    public int getTurnNumber() {
        return _turnNumber;
    }

    public Map<Zone, List<Integer>> getCardsInZones() {
        Map<Zone, List<Integer>> result = new HashMap<>();
        GameState gameState = _game.getGameState();
        for (Zone zone : gameState.getCardGroups().keySet()) {
            result.put(zone, new LinkedList<>());
            for (PhysicalCard card : gameState.getZoneCards(_playerId, zone)) {
                result.get(zone).add(card.getCardId());
            }
        }
        if (gameState instanceof ST1EGameState gameState1e) {
            result.put(Zone.SEED_DECK, new LinkedList<>());
            for (PhysicalCard card : gameState1e.getSeedDeck(_playerId)) {
                result.get(Zone.SEED_DECK).add(card.getCardId());
            }
        }
        return result;
    }

    public int getLastSyncedScore() {
        return _lastSyncedScore;
    }

    public void syncScore() {
        _lastSyncedScore = _currentScore;
    }

    public void incrementTurnNumber() {
        _turnNumber++;
    }

    public void setTurnNumber(int turnNumber) {
        _turnNumber = turnNumber;
    }

    public void setScore(int score) {
        _currentScore = score;
    }
}