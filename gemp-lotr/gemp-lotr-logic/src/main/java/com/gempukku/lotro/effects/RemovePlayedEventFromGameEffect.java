package com.gempukku.lotro.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.actions.lotronly.PlayEventAction;

import java.util.Collections;

public class RemovePlayedEventFromGameEffect extends AbstractEffect {
    private final PlayEventAction _action;

    public RemovePlayedEventFromGameEffect(PlayEventAction action) {
        _action = action;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Remove " + GameUtils.getFullName(_action.getEventPlayed()) + " from the game";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        Zone zone = _action.getEventPlayed().getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            LotroPhysicalCard eventPlayed = _action.getEventPlayed();
            game.getGameState().sendMessage(_action.getPerformingPlayer() + " removes " + GameUtils.getCardLink(eventPlayed) + " from the game");
            game.getGameState().removeCardsFromZone(eventPlayed.getOwner(), Collections.singletonList(eventPlayed));
            game.getGameState().addCardToZone(game, eventPlayed, Zone.REMOVED);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}