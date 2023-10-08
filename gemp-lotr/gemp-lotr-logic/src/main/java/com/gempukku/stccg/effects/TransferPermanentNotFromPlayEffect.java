package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class TransferPermanentNotFromPlayEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _targetCard;

    public TransferPermanentNotFromPlayEffect(PhysicalCard physicalCard, PhysicalCard targetCard) {
        _physicalCard = physicalCard;
        _targetCard = targetCard;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _targetCard.getZone().isInPlay()
                && !_physicalCard.getZone().isInPlay()
                && game.getModifiersQuerying().canHaveTransferredOn(game, _physicalCard, _targetCard);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(_physicalCard.getOwner() + " transfers " + GameUtils.getCardLink(_physicalCard) + " to " + GameUtils.getCardLink(_targetCard));
            gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
            gameState.attachCard(game, _physicalCard, _targetCard);

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
