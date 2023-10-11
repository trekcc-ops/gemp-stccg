package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.PlayEventResult;
import com.gempukku.stccg.rules.GameUtils;

public class CancelEventEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final PlayEventResult _effect;
    private final DefaultGame _game;

    public CancelEventEffect(ActionContext actionContext, PlayEventResult effectResult) {
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _effect = effectResult;
    }

    @Override
    public boolean isPlayableInFull() {
        return _effect.isEventNotCancelled();
    }

    @Override
    public String getText() {
        return "Cancel effect - " + GameUtils.getFullName(_effect.getPlayedCard());
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " cancels effect - " + GameUtils.getCardLink(_effect.getPlayedCard()));
            _effect.cancelEvent();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
