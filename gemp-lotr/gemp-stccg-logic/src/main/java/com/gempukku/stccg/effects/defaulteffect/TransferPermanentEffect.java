package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.CardTransferredResult;

public class TransferPermanentEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _targetCard;
    private final DefaultGame _game;

    public TransferPermanentEffect(DefaultGame game, PhysicalCard physicalCard, PhysicalCard targetCard) {
        _physicalCard = physicalCard;
        _targetCard = targetCard;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _targetCard.getZone().isInPlay()
                && _physicalCard.getZone().isInPlay()
                && _game.getModifiersQuerying().canBeTransferred(_game, _physicalCard)
                && _game.getModifiersQuerying().canHaveTransferredOn(_game, _physicalCard, _targetCard);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            gameState.sendMessage(_physicalCard.getOwnerName() + " transfers " + GameUtils.getCardLink(_physicalCard) + " to " + GameUtils.getCardLink(_targetCard));
            PhysicalCard transferredFrom = _physicalCard.getAttachedTo();
            gameState.transferCard(_physicalCard, _targetCard);

            _game.getActionsEnvironment().emitEffectResult(
                    new CardTransferredResult(_physicalCard, transferredFrom, _targetCard));

            afterTransferredCallback();

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void afterTransferredCallback() {

    }
}
