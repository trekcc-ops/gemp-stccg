package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDeckIntoHandOrDiscardEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final boolean _reveal;
    private final DefaultGame _game;

    public PutCardFromDeckIntoHandOrDiscardEffect(DefaultGame game, PhysicalCard physicalCard, boolean reveal) {
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
            if ((_game.getFormat().doesNotHaveRuleOfFour() || _game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(_game, _physicalCard.getOwner()))) {
                if(_reveal) {
                    gameState.sendMessage(_physicalCard.getOwner() + " puts " + GameUtils.getCardLink(_physicalCard) + " from deck into their hand");
                }
                else {
                    gameState.sendMessage(_physicalCard.getOwner() + " puts a card from deck into their hand");
                }
                gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
                gameState.addCardToZone(_game, _physicalCard, Zone.HAND);
                _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_physicalCard.getOwner()));
                return new FullEffectResult(true);
            } else {
                gameState.sendMessage(_physicalCard.getOwner() + " discards " + GameUtils.getCardLink(_physicalCard) + " from deck due to Rule of 4");
                gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
                gameState.addCardToZone(_game, _physicalCard, Zone.DISCARD);
            }
        }
        return new FullEffectResult(false);
    }
}
