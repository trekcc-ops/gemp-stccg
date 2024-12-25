package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collections;

public class PlacePlayedCardBeneathDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard _card;

    public PlacePlayedCardBeneathDrawDeckEffect(ActionContext actionContext) {
        super(actionContext);
        _card = actionContext.getSource();
    }

    public PlacePlayedCardBeneathDrawDeckEffect(PhysicalCard card) {
        super(card);
        _card = card;
    }

    @Override
    public String getText() {
        return "Put " + _card.getFullName() + " on bottom of your deck";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.sendMessage(_card.getOwnerName() + " puts " + _card.getCardLink() + " on bottom of their deck");
            _game.getGameState().removeCardsFromZone(_card.getOwnerName(), Collections.singletonList(_card));
            _game.getGameState().putCardOnBottomOfDeck(_card);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}