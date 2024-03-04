package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collections;

public class TransferPermanentNotFromPlayEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _targetCard;
    private final DefaultGame _game;

    public TransferPermanentNotFromPlayEffect(DefaultGame game, PhysicalCard physicalCard, PhysicalCard targetCard) {
        super(physicalCard.getOwnerName());
        _physicalCard = physicalCard;
        _targetCard = targetCard;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _targetCard.getZone().isInPlay()
                && !_physicalCard.getZone().isInPlay()
                && _game.getModifiersQuerying().canHaveTransferredOn(_game, _physicalCard, _targetCard);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            gameState.sendMessage(_physicalCard.getOwnerName() + " transfers " + _physicalCard.getCardLink() + " to " +
                    _targetCard.getCardLink());
            gameState.removeCardsFromZone(_physicalCard.getOwnerName(), Collections.singleton(_physicalCard));
            gameState.attachCard(_physicalCard, _targetCard);

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
