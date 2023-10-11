package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.PlayEventAction;

import java.util.Collections;

public class StackPlayedEventOnACardEffect extends DefaultEffect {
    private final PlayEventAction _action;
    private final PhysicalCard _stackOn;
    private final DefaultGame _game;

    public StackPlayedEventOnACardEffect(DefaultGame game, PlayEventAction action, PhysicalCard stackOn) {
        _action = action;
        _stackOn = stackOn;
        _game = game;
    }

    @Override
    public String getText() {
        return "Stack " + GameUtils.getFullName(_action.getEventPlayed()) + " on "+GameUtils.getFullName(_stackOn);
    }

    @Override
    public boolean isPlayableInFull() {
        Zone zone = _action.getEventPlayed().getZone();
        return _stackOn.getZone().isInPlay() && (zone == Zone.VOID || zone == Zone.VOID_FROM_HAND);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            PhysicalCard eventPlayed = _action.getEventPlayed();
            _game.getGameState().sendMessage(_action.getPerformingPlayer() + " stacks " + GameUtils.getCardLink(eventPlayed) + " on " + GameUtils.getCardLink(_stackOn));
            _game.getGameState().removeCardsFromZone(eventPlayed.getOwner(), Collections.singletonList(eventPlayed));
            _game.getGameState().stackCard(_game, eventPlayed, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}