package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.StackCardFromPlayEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.game.PlayConditions;

public class ChooseAndStackCardFromPlayEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable _stackOnFilter;
    private final Filterable[] _cardFilter;

    public ChooseAndStackCardFromPlayEffect(Action action, String playerId, Filterable stackOn, Filterable... filter) {
        _action = action;
        _playerId = playerId;
        _stackOnFilter = stackOn;
        _cardFilter = filter;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return PlayConditions.isActive(game, _cardFilter) && PlayConditions.isActive(game, _stackOnFilter);
    }

    @Override
    public void playEffect(DefaultGame game) {
        final SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new ChooseActiveCardEffect(_action.getActionSource(), _playerId, "Choose card to stack", _cardFilter) {
                    @Override
                    protected void cardSelected(DefaultGame game, final PhysicalCard cardToStack) {
                        subAction.appendEffect(
                                new ChooseActiveCardEffect(_action.getActionSource(), _playerId, "Choose card to stack on", _stackOnFilter) {
                                    @Override
                                    protected void cardSelected(DefaultGame game, PhysicalCard cardToStackOn) {
                                        subAction.appendEffect(
                                                new StackCardFromPlayEffect(cardToStack, cardToStackOn));
                                    }
                                });
                    }
                });
        processSubAction(game, subAction);
    }
}