package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.ActivateCardResult;

public class ActivateCardEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final DefaultGame _game;

    private final ActivateCardResult _activateCardResult;

    public ActivateCardEffect(DefaultGame game, PhysicalCard source) {
        _source = source;
        _game = game;

        _activateCardResult = new ActivateCardResult(_source);
    }

    public ActivateCardResult getActivateCardResult() {
        return _activateCardResult;
    }

    @Override
    public String getText() {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getActionsEnvironment().emitEffectResult(_activateCardResult);
        return new FullEffectResult(true);
    }
}
