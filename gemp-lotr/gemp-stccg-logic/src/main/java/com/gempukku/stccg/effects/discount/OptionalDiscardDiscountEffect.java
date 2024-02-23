package com.gempukku.stccg.effects.discount;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.effects.abstractsubaction.OptionalEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.PlayConditions;

import java.util.Collection;

public class OptionalDiscardDiscountEffect extends AbstractSubActionEffect implements DiscountEffect {
    private final int _discount;
    private boolean _paid;
    private int _minimalDiscount;
    private final String _playerId;
    private final int _discardCount;
    private final Filterable[] _discardFilters;
    private final Action _action;
    private final ActionContext _actionContext;

    public OptionalDiscardDiscountEffect(ActionContext actionContext, Action action, int discount, int discardCount,
                                         Filterable... discardFilters) {
        _playerId = actionContext.getPerformingPlayer();
        _action = action;
        _discount = discount;
        _discardCount = discardCount;
        _discardFilters = discardFilters;
        _actionContext = actionContext;
    }

    @Override
    public int getDiscountPaidFor() {
        return _paid ? _discount : 0;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _minimalDiscount = minimalDiscount;
    }

    @Override
    public String getText() {
        return "Discard cards to get a twilight discount";
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), _actionContext.getGame(), _discardCount, _discardFilters))
            return _minimalDiscount <= _discount;
        return _minimalDiscount == 0;
    }

    @Override
    public int getMaximumPossibleDiscount() {
        return PlayConditions.canDiscardFromPlay(_action.getActionSource(), _actionContext.getGame(),
                _discardCount, _discardFilters) ?
                    _discount : 0;
    }

    @Override
    public void playEffect() {
        if (isPlayableInFull()) {
            SubAction subAction = _action.createSubAction();
            if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), _actionContext.getGame(), _discardCount, _discardFilters))
                subAction.appendEffect(
                        new OptionalEffect(_actionContext.getGame(), subAction, _playerId,
                                new ChooseAndDiscardCardsFromPlayEffect(subAction, _playerId, _discardCount, _discardCount, _discardFilters) {
                                    @Override
                                    protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {
                                        if (cards.size() == _discardCount) {
                                            _paid = true;
                                            discountPaidCallback(_discardCount);
                                        }
                                    }
                                }));
            processSubAction(_actionContext.getGame(), subAction);
        }
    }

    protected void discountPaidCallback(int paid) {  }
}
