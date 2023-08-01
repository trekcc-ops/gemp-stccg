package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PutCardFromHandOnTopOfDeckEffect extends AbstractEffect {
    private final LotroPhysicalCard _physicalCard;
    private final boolean _reveal;

    public PutCardFromHandOnTopOfDeckEffect(LotroPhysicalCard physicalCard, boolean reveal) {
        _physicalCard = physicalCard;
        _reveal = reveal;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _physicalCard.getZone() == Zone.HAND;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put " + GameUtils.getFullName(_physicalCard) + " from hand on top of deck";
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            if(_reveal) {
                gameState.sendMessage(_physicalCard.getOwner() + " puts " + GameUtils.getCardLink(_physicalCard) + " from hand on top of their deck");
            }
            else {
                gameState.sendMessage(_physicalCard.getOwner() + " puts a card from hand on top of their deck");
            }
            gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
            gameState.putCardOnTopOfDeck(_physicalCard);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}