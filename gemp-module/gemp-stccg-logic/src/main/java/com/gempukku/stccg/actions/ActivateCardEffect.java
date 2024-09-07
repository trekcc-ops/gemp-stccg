package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.ActivateCardResult;

public class ActivateCardEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final DefaultGame _game;

    private final ActivateCardResult _activateCardResult;

    public ActivateCardEffect(DefaultGame game, PhysicalCard source) {
        super(source.getOwnerName());
        _source = source;
        _game = game;

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
