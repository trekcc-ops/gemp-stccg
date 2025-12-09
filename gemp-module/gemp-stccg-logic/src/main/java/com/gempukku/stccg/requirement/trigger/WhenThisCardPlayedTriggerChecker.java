package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class WhenThisCardPlayedTriggerChecker implements TriggerChecker {

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        final ActionResult actionResult = actionContext.getEffectResult(cardGame);
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            return actionResult instanceof PlayCardResult playCardResult &&
                    playCardResult.getPlayedCard() == actionContext.card();
        }
        return false;
    }


}