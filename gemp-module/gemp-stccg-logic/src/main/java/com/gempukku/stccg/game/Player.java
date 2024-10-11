package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Player {

    private final String _playerId;
    private boolean _decked;
    private int _score;
    private final Set<Affiliation> _playedAffiliations;
    private final DefaultGame _game;
    private final List<PhysicalCard> _cardsSeeded = new LinkedList<>();
    private final List<MissionCard> _solvedMissions = new LinkedList<>();

    public Player(DefaultGame game, String playerId) {
        _playerId = playerId;
        _decked = false;
        _score = 0;
        _playedAffiliations = new HashSet<>();
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
        _score += points;
    }

    public int getScore() {
        return _score;
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

    public List<PhysicalCard> getCardsSeeded() { return _cardsSeeded; }
    public void addCardSeeded(PhysicalCard card) { _cardsSeeded.add(card); }
    public void addSolvedMission(MissionCard card) { _solvedMissions.add(card); }
    public List<MissionCard> getSolvedMissions() { return _solvedMissions; }

    public boolean canLookOrRevealCardsInHandOfPlayer(String targetPlayerId) {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(targetPlayerId, _playerId);
    }
}
