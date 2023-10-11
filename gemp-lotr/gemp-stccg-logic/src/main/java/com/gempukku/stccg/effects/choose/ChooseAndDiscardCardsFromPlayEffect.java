package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;

public class ChooseAndDiscardCardsFromPlayEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private CostToEffectAction _resultSubAction;

    public ChooseAndDiscardCardsFromPlayEffect(DefaultGame game, Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(game, action.getActionSource(), playerId, "Choose cards to discard", minimum, maximum, filters);
        _action = action;
        _playerId = playerId;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        if (_action.getActionSource() == null)
            return Filters.any;
        return Filters.canBeDiscarded(_playerId, _action.getActionSource());
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new DiscardCardsFromPlayEffect(game, _playerId, _action.getActionSource(), Filters.in(cards)) {
            @Override
            protected void forEachDiscardedByEffectCallback(Collection<PhysicalCard> discardedCards) {
                ChooseAndDiscardCardsFromPlayEffect.this.forEachDiscardedByEffectCallback(discardedCards);
            }
        });
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
        cardsToBeDiscardedCallback(cards);
    }

    protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {

    }

    protected void forEachDiscardedByEffectCallback(Collection<PhysicalCard> cards) {

    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
