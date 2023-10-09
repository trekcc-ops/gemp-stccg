package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.ActivateCardResult;

public class CancelActivatedEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final ActivateCardResult _effect;

    public CancelActivatedEffect(PhysicalCard source, ActivateCardResult effect) {
        _source = source;
        _effect = effect;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return !_effect.isEffectCancelled();
    }

    @Override
    public String getText(DefaultGame game) {
        return "Cancel effect of " + GameUtils.getFullName(_effect.getSource());
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " cancels effect - " + GameUtils.getCardLink(_effect.getSource()));
            _effect.cancelEffect();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
