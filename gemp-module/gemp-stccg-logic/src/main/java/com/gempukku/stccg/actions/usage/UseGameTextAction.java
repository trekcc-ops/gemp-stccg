package com.gempukku.stccg.actions.usage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class UseGameTextAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;

    @JsonProperty("actionText")
    private final String _actionText;

    public UseGameTextAction(DefaultGame cardGame, PhysicalCard physicalCard, Player performingPlayer, String text) {
        super(cardGame, performingPlayer, text, ActionType.USE_GAME_TEXT);
        _performingCard = physicalCard;
        _actionText = text;
    }



    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;
        }
        Action nextAction = getNextAction();
        if (nextAction == null)
            setAsSuccessful();
        return nextAction;
    }

}