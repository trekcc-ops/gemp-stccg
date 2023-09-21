package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PlacePlayedCardBeneathDrawDeckEFfect extends AbstractEffect {
    private final LotroPhysicalCard card;

    public PlacePlayedCardBeneathDrawDeckEFfect(LotroPhysicalCard card) {
        this.card = card;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put " + GameUtils.getFullName(card) + " on bottom of your deck";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " on bottom of their deck");
            game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            game.getGameState().putCardOnBottomOfDeck(card);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}