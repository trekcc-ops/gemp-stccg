package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.defaulteffect.StackCardFromPlayEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class ChooseAndStackCardFromPlayEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable _stackOnFilter;
    private final Filterable[] _cardFilter;
    private final DefaultGame _game;

    public ChooseAndStackCardFromPlayEffect(DefaultGame game, Action action, String playerId, Filterable stackOn, Filterable... filter) {
        _action = action;
        _playerId = playerId;
        _stackOnFilter = stackOn;
        _cardFilter = filter;
        _game = game;
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
        return (Filters.countActive(_game, _cardFilter) >= 1) && (Filters.countActive(_game, _stackOnFilter) >= 1);
    }

    @Override
    public void playEffect() {
        final SubAction subAction = _action.createSubAction();
        subAction.appendEffect(
                new ChooseActiveCardEffect(_action.getActionSource(), _playerId, "Choose card to stack", _cardFilter) {
                    @Override
                    protected void cardSelected(final PhysicalCard cardToStack) {
                        subAction.appendEffect(
                                new ChooseActiveCardEffect(_action.getActionSource(), _playerId, "Choose card to stack on", _stackOnFilter) {
                                    @Override
                                    protected void cardSelected(PhysicalCard cardToStackOn) {
                                        subAction.appendEffect(
                                                new StackCardFromPlayEffect(_game, cardToStack, cardToStackOn));
                                    }
                                });
                    }
                });
        processSubAction(_game, subAction);
    }
}