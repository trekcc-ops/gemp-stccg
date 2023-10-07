package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.results.ActivateCardResult;

public class ActivateCardEffect extends AbstractEffect<DefaultGame> {
    private final PhysicalCard _source;

    private final ActivateCardResult _activateCardResult;

    public ActivateCardEffect(PhysicalCard source) {
        _source = source;

        _activateCardResult = new ActivateCardResult(_source);
    }

    public ActivateCardResult getActivateCardResult() {
        return _activateCardResult;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        game.getActionsEnvironment().emitEffectResult(_activateCardResult);
        return new FullEffectResult(true);
    }
}
