package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

public abstract class ChooseCardsFromZoneEffect extends DefaultEffect {
    private final String _performingPlayer;
    private final String _zoneOwner;
    private final int _minimum;
    private int _maximum;
    private final Filter _filter;
    protected final Zone _fromZone;

    public ChooseCardsFromZoneEffect(DefaultGame game, Zone zone, String playerId, int minimum, int maximum, Filterable... filters) {
        this(game, zone, playerId, playerId, minimum, maximum, filters);
    }

    public ChooseCardsFromZoneEffect(DefaultGame game, Zone zone, String playerId, String deckId, int minimum, int maximum, Filterable... filters) {
        super(game, playerId);
        _performingPlayer = playerId;
        _zoneOwner = deckId;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
        _fromZone = zone;
    }

    @Override
    public String getText() {
        return "Choose card from " + _fromZone.getHumanReadable();
    }

    @Override
    public boolean isPlayableInFull() {
        if (_fromZone == Zone.DRAW_DECK) {
            return Filters.filter(_game.getGameState().getDrawDeck(_zoneOwner), _game, _filter).size() >= _minimum;
        } else if (_fromZone == Zone.DISCARD) {
            return Filters.filter(_game.getGameState().getDiscard(_zoneOwner), _game, _filter).size() >= _minimum;
        } else if (_fromZone == Zone.HAND) {
            return Filters.filter(_game.getGameState().getHand(_zoneOwner), _game, _filter).size() >= _minimum;
        }
        return false;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            Collection<PhysicalCard> cards = null;
            if (_fromZone == Zone.DRAW_DECK) {
                cards = Filters.filter(_game.getGameState().getDrawDeck(_zoneOwner), _game, _filter);
            } else if (_fromZone == Zone.DISCARD) {
                cards = Filters.filter(_game.getGameState().getDiscard(_zoneOwner), _game, _filter);
            } else if (_fromZone == Zone.HAND) {
                cards = Filters.filter(_game.getGameState().getHand(_zoneOwner), _game, _filter);
            }

            assert cards != null;
            if (_fromZone == Zone.HAND)
                _maximum = Math.min(_maximum, cards.size());

            if (_maximum == 0) {
                cardsSelected(_game, Collections.emptySet());
            } else if (cards.size() == _minimum) {
                cardsSelected(_game, cards);
            } else if (_fromZone == Zone.HAND) {
                _game.getUserFeedback().sendAwaitingDecision(_performingPlayer,
                        new CardsSelectionDecision(1, getText(), cards, _minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                cardsSelected(_game, selectedCards);
                            }
                        }
                );
            } else {
                _game.getUserFeedback().sendAwaitingDecision(_performingPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Choose card from " + _fromZone.getHumanReadable(),
                                new LinkedList<>(cards), _minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                cardsSelected(_game, getSelectedCardsByResponse(result));
                            }
                        });
            }
            return new FullEffectResult(true);
        } else return new FullEffectResult(false);
    }

    protected abstract void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards);
}
