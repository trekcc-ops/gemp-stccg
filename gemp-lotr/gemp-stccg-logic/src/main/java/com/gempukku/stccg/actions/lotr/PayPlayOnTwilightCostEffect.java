package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PayPlayOnTwilightCostEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _target;
    private final int _twilightModifier;
    private final DefaultGame _game;

    public PayPlayOnTwilightCostEffect(DefaultGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightModifier) {
        super(target.getOwnerName());
        _physicalCard = physicalCard;
        _target = target;
        _twilightModifier = twilightModifier;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (!currentPlayerId.equals(_physicalCard.getOwnerName())) {
            int twilightPool = _game.getGameState().getTwilightPool();
            return twilightPool >= twilightCost;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwnerName())) {
            _game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                _game.getGameState().sendMessage(_physicalCard.getOwnerName() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            int twilightPool = _game.getGameState().getTwilightPool();
            boolean success = twilightPool >= twilightCost;
            twilightCost = Math.min(twilightPool, twilightCost);
            _game.getGameState().removeTwilight(twilightCost);
            if (twilightCost > 0)
                _game.getGameState().sendMessage(_physicalCard.getOwnerName() + " removes " + twilightCost + " from twilight pool");
            return new FullEffectResult(success);
        }
    }
}
