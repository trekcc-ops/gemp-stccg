package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class WhenThisCardPlayedTriggerChecker implements TriggerChecker {

    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return cardGame.getCurrentActionResult() instanceof PlayCardResult playCardResult &&
                playCardResult.hasType(ActionResult.Type.JUST_PLAYED) &&
                playCardResult.getPlayedCard() == actionContext.card();
    }


}