package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.rules.GameUtils;

import java.util.Collections;

public class PutCardFromDiscardOnBottomOfDeckEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;

    public PutCardFromDiscardOnBottomOfDeckEffect(PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _physicalCard.getZone() == Zone.DISCARD;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(_physicalCard.getOwner() + " puts " + GameUtils.getCardLink(_physicalCard) + " from discard on the bottom of deck");
            gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
            gameState.putCardOnBottomOfDeck(_physicalCard);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put " + GameUtils.getFullName(_physicalCard) + " from discard on bottom of deck";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }
}