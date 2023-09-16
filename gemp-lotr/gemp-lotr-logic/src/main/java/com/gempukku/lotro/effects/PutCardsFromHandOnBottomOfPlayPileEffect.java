package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Arrays;

public class PutCardsFromHandOnBottomOfPlayPileEffect extends AbstractEffect {
    private final LotroPhysicalCard[] physicalCards;
    private final boolean _reveal;

    public PutCardsFromHandOnBottomOfPlayPileEffect(boolean reveal, LotroPhysicalCard... physicalCard) {
        physicalCards = physicalCard;
        _reveal = reveal;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        for (LotroPhysicalCard card : physicalCards) {
            if (card.getZone() != Zone.HAND)
                return false;
        }

        return true;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put card from hand on bottom of deck";
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.removeCardsFromZone(physicalCards[0].getOwner(), Arrays.asList(physicalCards));
            for (LotroPhysicalCard physicalCard : physicalCards) {
                if(_reveal) {
                    gameState.sendMessage(physicalCard.getOwner() + " puts " + GameUtils.getCardLink(physicalCard) + " from hand on bottom of their play pile");
                }
                else {
                    gameState.sendMessage(physicalCard.getOwner() + " puts a card from hand on bottom of their play pile");
                }
                gameState.addCardToZone(null, physicalCard, Zone.PLAY_PILE, false);
            }
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
