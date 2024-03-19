package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class CheckPhaseLimitEffect extends UnrespondableEffect {
    private final Action _action;
    private final PhysicalCard _card;
    private final String _limitPrefix;
    private final int _limit;
    private final Phase _phase;
    private final Effect _limitedEffect;
    private final DefaultGame _game;

    public CheckPhaseLimitEffect(DefaultGame game, Action action, PhysicalCard card, String limitPrefix, int limit, Phase phase, Effect limitedEffect) {
        _card = card;
        _limitPrefix = limitPrefix;
        _limit = limit;
        _phase = phase;
        _limitedEffect = limitedEffect;
        _action = action;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        Phase phase = _phase;
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();

        int incrementedBy = _game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_card, _limitPrefix, phase).incrementToLimit(_limit, 1);
        if (incrementedBy > 0) {
            SubAction subAction = _action.createSubAction();
            subAction.appendEffect(
                    _limitedEffect);
            _game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
