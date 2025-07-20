package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class YesNoDecision extends MultipleChoiceAwaitingDecision {

    public YesNoDecision(Player player, DecisionContext context, DefaultGame cardGame) {
        super(player, context, new String[]{"Yes", "No"}, cardGame);
    }

    @Override
    public void followUp() {
        if (_selectedIndex == 0)
            yes();
        else
            no();
    }

    protected void yes() {

    }

    protected void no() {

    }
}