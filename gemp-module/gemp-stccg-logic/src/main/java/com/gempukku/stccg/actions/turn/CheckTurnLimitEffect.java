package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class CheckTurnLimitEffect extends DefaultEffect implements UsageEffect {
    private final PhysicalCard _card;
    private final int _limit;
    private final String _prefix;

    public CheckTurnLimitEffect(DefaultGame game, Action action, int limit) {
        super(game, action);
        _card = action.getActionSource();
        _limit = limit;
        _prefix = action.getCardActionPrefix();
    }


    public boolean isPlayableInFull() {
        return _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _prefix).getUsedLimit() < _limit;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        int incrementedBy = _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _prefix)
                .incrementToLimit(_limit, 1);
        return new FullEffectResult(incrementedBy > 0);
    }
}