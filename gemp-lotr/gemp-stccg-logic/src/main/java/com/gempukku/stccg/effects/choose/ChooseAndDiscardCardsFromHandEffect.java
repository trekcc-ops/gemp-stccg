package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ChooseAndDiscardCardsFromHandEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final boolean _forced;
    private final Evaluator _minimum;
    private final Evaluator _maximum;
    private final Filterable[] _filter;
    private final DefaultGame _game;
    private String _text = "Choose cards to discard";

    public ChooseAndDiscardCardsFromHandEffect(DefaultGame game, Action action, String playerId, boolean forced, int count, Filterable... filters) {
        this(game, action, playerId, forced, count, count, filters);
    }

    public ChooseAndDiscardCardsFromHandEffect(DefaultGame game, Action action, String playerId, boolean forced, 
                                               int minimum, int maximum, Filterable... filters) {
        _action = action;
        _playerId = playerId;
        _forced = forced;
        _minimum = new ConstantEvaluator(minimum);
        _maximum = new ConstantEvaluator(maximum);
        _filter = filters;
        _game = game;
    }
    
    public void setText(String text) {
        _text = text;
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
        return Filters.filter(_game.getGameState().getHand(_playerId), _game, _filter).size()
                >= _minimum.evaluateExpression(_game, null);
    }

    @Override
    public void playEffect() {
        if (_forced && !_game.getModifiersQuerying().canDiscardCardsFromHand(_playerId, _action.getActionSource()))
            return;

        Collection<PhysicalCard> hand = Filters.filter(_game.getGameState().getHand(_playerId), _game, _filter);
        int maximum = Math.min(_maximum.evaluateExpression(_game, null), hand.size());

        int minimum = _minimum.evaluateExpression(_game, null);
        if (maximum == 0) {
            cardsBeingDiscardedCallback(Collections.emptySet());
        } else if (hand.size() <= minimum) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(new DiscardCardsFromZoneEffect(_game, _action.getActionSource(), Zone.HAND, _playerId, hand, _forced));
            processSubAction(_game, subAction);
            cardsBeingDiscardedCallback(hand);
        } else {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, _text, hand, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                            SubAction subAction = _action.createSubAction();
                            subAction.appendEffect(new DiscardCardsFromZoneEffect(_game, _action.getActionSource(), Zone.HAND, _playerId, cards, _forced));
                            processSubAction(_game, subAction);
                            cardsBeingDiscardedCallback(cards);
                        }
                    });
        }
    }

    protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
    }
}
