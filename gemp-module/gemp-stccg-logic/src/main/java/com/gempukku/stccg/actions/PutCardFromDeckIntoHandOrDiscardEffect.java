package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.draw.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDeckIntoHandOrDiscardEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final boolean _reveal;
    private final DefaultGame _game;

    public PutCardFromDeckIntoHandOrDiscardEffect(DefaultGame game, PhysicalCard physicalCard, boolean reveal) {
        super(physicalCard.getOwnerName());
        _physicalCard = physicalCard;
        _reveal = reveal;
        _game = game;
    }

    public PhysicalCard getCard() {
        return _physicalCard;
    }

    @Override
    public String getText() {
        return "Put card from deck into hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return _physicalCard.getZone() == Zone.DRAW_DECK;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_physicalCard.getZone() == Zone.DRAW_DECK) {
            var gameState = _game.getGameState();
            if(_reveal) {
                gameState.sendMessage(_physicalCard.getOwnerName() + " puts " + _physicalCard.getCardLink() + " from deck into their hand");
            }
            else {
                gameState.sendMessage(_physicalCard.getOwnerName() + " puts a card from deck into their hand");
            }
            gameState.removeCardsFromZone(_physicalCard.getOwnerName(), Collections.singleton(_physicalCard));
            gameState.addCardToZone(_physicalCard, Zone.HAND);
            _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(this, _physicalCard));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
