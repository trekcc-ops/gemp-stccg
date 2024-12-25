package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.AbstractSubActionEffect;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ChooseAndDiscardCardsFromHandEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final boolean _forced;
    private final Filterable[] _filter;
    private final int _minimum;
    private final int _maximum;
    private final String _text = "Choose cards to discard";

    public ChooseAndDiscardCardsFromHandEffect(DefaultGame game, Action action, String playerId, boolean forced, int count, Filterable... filters) {
        this(game, action, playerId, forced, count, count, filters);
    }

    public ChooseAndDiscardCardsFromHandEffect(DefaultGame game, Action action, String playerId, boolean forced, 
                                               int minimum, int maximum, Filterable... filters) {
        super(game);
        _action = action;
        _playerId = playerId;
        _forced = forced;
        _minimum = minimum;
        _maximum = maximum;
        _filter = filters;
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.filter(_game.getGameState().getHand(_playerId), _game, _filter).size() >= _minimum;
    }

    @Override
    public void playEffect() {
        if (_forced && !_game.getModifiersQuerying().canDiscardCardsFromHand(_playerId, _action.getPerformingCard()))
            return;

        Collection<PhysicalCard> hand = Filters.filter(_game.getGameState().getHand(_playerId), _game, _filter);
        int maximum = Math.min(_maximum, hand.size());

        if (maximum == 0) {
            cardsBeingDiscardedCallback(Collections.emptySet());
        } else if (hand.size() <= _minimum) {
            SubAction subAction = new SubAction(_action, _game);
            subAction.appendEffect(new DiscardCardsFromZoneEffect(_game, _action.getPerformingCard(), Zone.HAND, _playerId, hand, _forced));
            processSubAction(_game, subAction);
            cardsBeingDiscardedCallback(hand);
        } else {
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(_game.getPlayer(_playerId), _text, hand, _minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                            SubAction subAction = new SubAction(_action, _game);
                            subAction.appendEffect(new DiscardCardsFromZoneEffect(_game, _action.getPerformingCard(), Zone.HAND, _playerId, cards, _forced));
                            processSubAction(_game, subAction);
                            cardsBeingDiscardedCallback(cards);
                        }
                    });
        }
    }

    protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
    }
}