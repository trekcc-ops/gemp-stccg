package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class AllowResponsesAction extends SystemQueueAction {

    private final EffectResult.Type _type;
    private final Action _actionBeingRespondedTo;

    public AllowResponsesAction(DefaultGame game, Action actionBeingRespondedTo, EffectResult.Type type) {
        super(game);
        _type = type;
        _actionBeingRespondedTo = actionBeingRespondedTo;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return null;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        cardGame.getActionsEnvironment().emitEffectResult(new EffectResult(_type));
        return getNextAction();
    }
}