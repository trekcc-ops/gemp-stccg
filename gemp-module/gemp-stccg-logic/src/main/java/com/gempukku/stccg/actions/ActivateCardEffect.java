package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.ActivateCardResult;

public class ActivateCardEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final ActivateCardResult _activateCardResult;

    public ActivateCardEffect(PhysicalCard source) {
        super(source);
        _source = source;

        _activateCardResult = new ActivateCardResult(this);
    }

    public ActivateCardResult getActivateCardResult() {
        return _activateCardResult;
    }

    @Override
    public String getText() {
        return "Activated " + _source.getCardLink();
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
