package com.gempukku.stccg.effects.discount;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;

public class DiscardCardFromHandDiscountEffect extends AbstractSubActionEffect implements DiscountEffect {
    private final Action _action;
    private final String _playerId;
    private int _minimalDiscount;
    private int _discardedCount;
    private final Filterable[] _discardedCardFilter;
    private final ActionContext _actionContext;

    public DiscardCardFromHandDiscountEffect(ActionContext actionContext, Action action,
                                             Filterable... discardedCardFilter) {
        _playerId = actionContext.getPerformingPlayer();
        _action = action;
        _discardedCardFilter = discardedCardFilter;
        _actionContext = actionContext;
    }

    @Override
    public int getDiscountPaidFor() {
        return _discardedCount;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _minimalDiscount = minimalDiscount;
    }

    @Override
    public String getText() {
        return "Discard cards to reduce twilight cost";
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public int getMaximumPossibleDiscount() {
        return Filters.filter(_actionContext.getGameState().getHand(_playerId), _actionContext.getGame(), _discardedCardFilter).size();
    }

    @Override
    public boolean isPlayableInFull() {
        return Filters.filter(_actionContext.getGameState().getHand(_playerId), _actionContext.getGame(), _discardedCardFilter).size() >= _minimalDiscount;
    }

    @Override
    public void playEffect() {
        if (isPlayableInFull()) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(
                    new ChooseAndDiscardCardsFromHandEffect(_actionContext.getGame(), _action, _playerId, false, _minimalDiscount, Integer.MAX_VALUE, _discardedCardFilter) {
                        @Override
                        protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
                            _discardedCount = cardsBeingDiscarded.size();
                            discountPaidCallback(_discardedCount);
                        }
                    });
            processSubAction(_actionContext.getGame(), subAction);
        }
    }

    protected void discountPaidCallback(int paid) {  }
}
