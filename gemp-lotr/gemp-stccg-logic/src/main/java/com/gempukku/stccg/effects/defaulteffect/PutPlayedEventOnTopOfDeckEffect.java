package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.PlayEventAction;

import java.util.Collections;

public class PutPlayedEventOnTopOfDeckEffect extends DefaultEffect {
    private final PlayEventAction _action;
    private final DefaultGame _game;

    public PutPlayedEventOnTopOfDeckEffect(DefaultGame game, PlayEventAction action) {
        _action = action;
        _game = game;
    }

    @Override
    public String getText() {
        return "Put " + GameUtils.getFullName(_action.getEventPlayed()) + " on top of your deck";
    }

    @Override
    public boolean isPlayableInFull() {
        Zone zone = _action.getEventPlayed().getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            PhysicalCard eventPlayed = _action.getEventPlayed();
            _game.getGameState().sendMessage(_action.getPerformingPlayer() + " puts " + GameUtils.getCardLink(eventPlayed) + " on top of their deck");
            _game.getGameState().removeCardsFromZone(eventPlayed.getOwner(), Collections.singletonList(eventPlayed));
            _game.getGameState().putCardOnTopOfDeck(eventPlayed);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
