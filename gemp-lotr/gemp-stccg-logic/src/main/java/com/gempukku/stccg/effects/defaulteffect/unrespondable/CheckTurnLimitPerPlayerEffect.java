package com.gempukku.stccg.effects.defaulteffect.unrespondable;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

public class CheckTurnLimitPerPlayerEffect extends UnrespondableEffect {
    private final Action _action;
    private final PhysicalCard _card;
    private final String _playerId;
    private final int _limit;
    private final Effect _limitedEffect;
    private final DefaultGame _game;

    public CheckTurnLimitPerPlayerEffect(DefaultGame game, Action action, PhysicalCard card, String playerId, int limit, Effect limitedEffect) {
        _card = card;
        this._playerId = playerId;
        _limit = limit;
        _limitedEffect = limitedEffect;
        _action = action;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        int incrementedBy = _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _playerId +"-").incrementToLimit(_limit, 1);
        if (incrementedBy > 0) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(
                    _limitedEffect);
            _game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}