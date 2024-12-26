package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class AllowResponsesAction extends SystemQueueAction {

    private final EffectResult.Type _type;

    public AllowResponsesAction(DefaultGame game, EffectResult.Type type) {
        super(game);
        _type = type;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        cardGame.getActionsEnvironment().emitEffectResult(new EffectResult(_type));
        return getNextAction();
    }
}