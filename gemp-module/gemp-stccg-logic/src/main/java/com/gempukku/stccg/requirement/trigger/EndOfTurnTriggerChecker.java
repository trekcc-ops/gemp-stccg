package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfTurnTriggerChecker implements TriggerChecker {

        public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
            return cardGame.isCurrentActionResultType(ActionResult.Type.END_OF_TURN);
        }

}