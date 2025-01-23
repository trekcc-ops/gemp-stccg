package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.CardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "playerId", "score", "turnNumber", "decked" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class Player {
    @JsonProperty("playerId")
    private final String _playerId;
    @JsonProperty("decked")
    private boolean _decked;
    private final Collection<Affiliation> _playedAffiliations = EnumSet.noneOf(Affiliation.class);
    @JsonProperty("cardGroups")
    Map<Zone, CardGroup> _cardGroups = new HashMap<>();
    @JsonProperty("score")
    private int _currentScore;
    private int _lastSyncedScore;
    @JsonProperty("turnNumber")
    private int _turnNumber;

    public Player(String playerId) {
        _playerId = playerId;
        _decked = false;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public boolean isDecked() {
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

    public boolean hasCardInZone(DefaultGame cardGame, Zone zone, int count, Filterable... cardFilter) {
        if (zone == Zone.HAND)
            return Filters.filter(getCardsInGroup(Zone.HAND), cardGame, cardFilter).size() >= count;
        else if (zone == Zone.DISCARD)
            return Filters.filter(getCardsInGroup(Zone.DISCARD), cardGame, cardFilter).size() >= count;
        else
            return false;
    }


    public boolean hasACopyOfCardInPlay(DefaultGame cardGame, PhysicalCard card) {
        for (PhysicalCard cardInPlay : cardGame.getGameState().getAllCardsInPlay()) {
            if (cardInPlay.isCopyOf(card) && cardInPlay.getOwner() == this)
                return true;
        }
        return false;
    }

    public int getScore() {
        return _currentScore;
    }

    public List<PhysicalCard> getCardsInHand() {
        return getCardsInGroup(Zone.HAND);
    }

    public List<PhysicalCard> getCardsInDrawDeck() {
        return getCardsInGroup(Zone.DRAW_DECK);
    }

    public Collection<PhysicalCard> getDiscardPile() {
        return getCardsInGroup(Zone.DISCARD);
    }

    public Collection<PhysicalCard> getRemovedPile() {
        return getCardsInGroup(Zone.REMOVED);
    }

    public int getTurnNumber() {
        return _turnNumber;
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

    public void addCardGroup(Zone zone) {
        _cardGroups.put(zone, new CardGroup(zone));
    }

    public List<PhysicalCard> getCardGroup(Zone zone) {
        return _cardGroups.get(zone).getCards();
    }

    public void addCardToGroup(Zone zone, PhysicalCard card) throws InvalidGameLogicException {
        CardGroup group = _cardGroups.get(zone);
        if (group != null) {
            group.addCard(card);
        } else {
            throw new InvalidGameLogicException("Cannot add card to zone " + zone);
        }
    }

    public List<PhysicalCard> getCardsInGroup(Zone zone) {
        CardGroup group = _cardGroups.get(zone);
        if (group == null) {
            return new LinkedList<>();
        } else {
            return Collections.unmodifiableList(group.getCards());
        }
    }

    public void shuffleDrawDeck(DefaultGame cardGame) {
        if (!cardGame.getFormat().isNoShuffle())
            Collections.shuffle(_cardGroups.get(Zone.DRAW_DECK).getCards(), ThreadLocalRandom.current());
    }

    public void setCardGroup(Zone zone, List<PhysicalCard> subDeck) throws InvalidGameLogicException {
        CardGroup group = _cardGroups.get(zone);
        if (group != null) {
            group.setCards(subDeck);
        } else {
            throw new InvalidGameLogicException("Unable to set cards for " + zone);
        }
    }

    public Set<Zone> getCardGroupZones() {
        return _cardGroups.keySet();
    }

}