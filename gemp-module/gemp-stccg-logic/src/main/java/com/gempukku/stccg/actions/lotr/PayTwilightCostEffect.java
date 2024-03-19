package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PayTwilightCostEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final int _twilightModifier;
    private final boolean _ignoreRoamingPenalty;
    private final DefaultGame _game;

    public PayTwilightCostEffect(DefaultGame game, PhysicalCard physicalCard) {
        this(game, physicalCard, 0);
    }

    public PayTwilightCostEffect(DefaultGame game, PhysicalCard physicalCard, int twilightModifier) {
        this(game, physicalCard, twilightModifier, false);
    }

    public PayTwilightCostEffect(DefaultGame game, PhysicalCard physicalCard, int twilightModifier, boolean ignoreRoamingPenalty) {
        super(physicalCard.getOwnerName());
        _game = game;
        _physicalCard = physicalCard;
        _twilightModifier = twilightModifier;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
    }

    @Override
    public boolean isPlayableInFull() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, null, _twilightModifier, _ignoreRoamingPenalty);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (!currentPlayerId.equals(_physicalCard.getOwnerName())) {
            int twilightPool = _game.getGameState().getTwilightPool();
            return twilightPool >= twilightCost;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, null, _twilightModifier, _ignoreRoamingPenalty);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwnerName())) {
            _game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                _game.sendMessage(_physicalCard.getOwnerName() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            boolean success = _game.getGameState().getTwilightPool() >= twilightCost;
            twilightCost = Math.min(twilightCost, _game.getGameState().getTwilightPool());
            if (twilightCost > 0) {
                _game.getGameState().removeTwilight(twilightCost);
                _game.sendMessage(_physicalCard.getOwnerName() + " removes " + twilightCost + " from twilight pool");
            }
            return new FullEffectResult(success);
        }
    }
}
