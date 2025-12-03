package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfTurnTriggerChecker implements TriggerChecker {

        @Override
        public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
            return actionContext.hasActionResultType(cardGame, ActionResult.Type.END_OF_TURN);
        }

}