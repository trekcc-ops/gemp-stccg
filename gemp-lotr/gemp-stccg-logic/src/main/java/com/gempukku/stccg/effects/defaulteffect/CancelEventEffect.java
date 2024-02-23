package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.PlayEventResult;
import com.gempukku.stccg.rules.GameUtils;

public class CancelEventEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final PlayEventResult _effectResult;
    private final DefaultGame _game;
    private final PhysicalCard _playedCard;

    public CancelEventEffect(ActionContext actionContext, PlayEventResult effectResult) {
        _source = actionContext.getSource();
        _game = actionContext.getGame();
        _effectResult = effectResult;
        _playedCard = effectResult.getPlayedCard();
    }

    @Override
    public boolean isPlayableInFull() {
        return _effectResult.isEventNotCancelled();
    }

    @Override
    public String getText() {
        return "Cancel effect - " + _playedCard.getFullName();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(_source.getCardLink() + " cancels effect - " +
                    _playedCard.getCardLink());
            _effectResult.cancelEvent();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
