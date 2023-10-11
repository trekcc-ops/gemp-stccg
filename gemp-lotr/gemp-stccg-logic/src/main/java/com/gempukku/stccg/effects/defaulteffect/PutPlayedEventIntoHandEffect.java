package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class PutPlayedEventIntoHandEffect extends DefaultEffect {
    private final PhysicalCard card;
    private final DefaultGame _game;

    public PutPlayedEventIntoHandEffect(ActionContext actionContext) {
        this.card = actionContext.getSource();
        _game = actionContext.getGame();
    }

    @Override
    public String getText() {
        return "Put " + GameUtils.getFullName(card) + " into hand";
    }

    @Override
    public boolean isPlayableInFull() {
        Zone zone = card.getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " into hand");
            _game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            _game.getGameState().addCardToZone(_game, card, Zone.HAND);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}