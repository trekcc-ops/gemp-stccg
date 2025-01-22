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
@JsonIncludeProperties({ "playerId", "score", "turnNumber", "cardsInZones", "decked" })
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="playerId")
public class Player {
    @JsonProperty("playerId")
    private final String _playerId;
    @JsonProperty("decked")
    private boolean _decked;
    private final Collection<Affiliation> _playedAffiliations = EnumSet.noneOf(Affiliation.class);
    Map<Zone, CardGroup> _cardZoneGroups = new HashMap<>();
    private final DefaultGame _game;
    @JsonProperty("score")
    private int _currentScore;
    private int _lastSyncedScore;
    @JsonProperty("turnNumber")
    private int _turnNumber;
    Map<Zone, List<PhysicalCard>> _cardGroups = new HashMap<>();

    public Player(DefaultGame game, String playerId) {
        _playerId = playerId;
        _decked = false;
        _game = game;
    }

    public DefaultGame getGame() { return _game; }

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

    public boolean hasCardInZone(Zone zone, int count, Filterable... cardFilter) {
        if (zone == Zone.HAND)
            return Filters.filter(getCardsInGroup(Zone.HAND), _game, cardFilter).size() >= count;
        else if (zone == Zone.DISCARD)
            return Filters.filter(getCardsInGroup(Zone.DISCARD), _game, cardFilter).size() >= count;
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

    public List<PhysicalCard> getCardsInHand() {
        return getCardsInGroup(Zone.HAND);
    }

    public List<PhysicalCard> getCardsInDrawDeck() {
        return getCardsInGroup(Zone.DRAW_DECK);
    }

    public Collection<PhysicalCard> getDiscardPile() {
        return _game.getGameState().getDiscard(_playerId);
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
        _cardZoneGroups.put(zone, new CardGroup(zone));
    }

    public List<PhysicalCard> getCardGroup(Zone zone) {
        return _cardGroups.get(zone);
    }

    public void addCardToGroup(Zone zone, PhysicalCard card) throws InvalidGameLogicException {
        CardGroup group = _cardZoneGroups.get(zone);
        if (group != null) {
            group.addCard(card);
        } else {
            throw new InvalidGameLogicException("Cannot add card to zone " + zone);
        }
    }

    public List<PhysicalCard> getCardsInGroup(Zone zone) {
        CardGroup group = _cardZoneGroups.get(zone);
        if (group == null) {
            return new LinkedList<>();
        } else {
            return Collections.unmodifiableList(group.getCards());
        }
    }

    public void shuffleDrawDeck(DefaultGame cardGame) {
        if (!cardGame.getFormat().isNoShuffle())
            Collections.shuffle(_cardGroups.get(Zone.DRAW_DECK), ThreadLocalRandom.current());
    }

    public void setCardGroup(Zone zone, List<PhysicalCard> subDeck) {
        _cardGroups.put(zone, subDeck);
    }

    public Set<Zone> getCardGroupZones() {
        return _cardGroups.keySet();
    }

}