package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfTurnTriggerChecker implements TriggerChecker {
        @Override
        public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
            return actionContext.hasActionResultType(cardGame, ActionResult.Type.START_OF_TURN);
        }

}