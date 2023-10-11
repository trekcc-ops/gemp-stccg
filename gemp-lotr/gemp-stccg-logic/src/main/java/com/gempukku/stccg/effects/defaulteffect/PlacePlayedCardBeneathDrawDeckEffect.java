package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class PlacePlayedCardBeneathDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard card;
    private final DefaultGame _game;

    public PlacePlayedCardBeneathDrawDeckEffect(ActionContext actionContext) {
        this.card = actionContext.getSource();
        _game = actionContext.getGame();
    }

    public PlacePlayedCardBeneathDrawDeckEffect(DefaultGame game, PhysicalCard card) {
        this.card = card;
        _game = game;
    }

    @Override
    public String getText() {
        return "Put " + GameUtils.getFullName(card) + " on bottom of your deck";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " on bottom of their deck");
            _game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            _game.getGameState().putCardOnBottomOfDeck(card);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}