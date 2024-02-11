package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.ActivateCardResult;

public class CancelActivatedEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final ActivateCardResult _effect;
    private final DefaultGame _game;

    public CancelActivatedEffect(DefaultGame game, PhysicalCard source, ActivateCardResult effect) {
        _source = source;
        _effect = effect;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return !_effect.isEffectCancelled();
    }

    @Override
    public String getText() {
        return "Cancel effect of " + _effect.getSource().getFullName();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " cancels effect - " + GameUtils.getCardLink(_effect.getSource()));
            _effect.cancelEffect();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
