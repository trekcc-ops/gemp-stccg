package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.RemoveCardsFromTheGameEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;

import java.util.Collection;

public class ChooseAndRemoveFromTheGameCardsInPlayEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private CostToEffectAction _resultSubAction;

    public ChooseAndRemoveFromTheGameCardsInPlayEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to remove from play", minimum, maximum, filters);
        _action = action;
        _playerId = playerId;
    }

    @Override
    protected void cardsSelected(Collection<PhysicalCard> cards) {
        _resultSubAction = _action.createSubAction();
        _resultSubAction.appendEffect(new RemoveCardsFromTheGameEffect(_game, _playerId, _action.getActionSource(), cards));
        _game.getActionsEnvironment().addActionToStack(_resultSubAction);
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
