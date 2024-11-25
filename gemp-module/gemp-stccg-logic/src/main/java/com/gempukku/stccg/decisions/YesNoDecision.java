package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.Player;

public class YesNoDecision extends MultipleChoiceAwaitingDecision {

    public YesNoDecision(Player player, String text) {
        super(player, text, new String[]{"Yes", "No"});
    }


    @Override
    protected final void validDecisionMade(int index, String result) {
        if (index == 0)
            yes();
        else
            no();
    }

    protected void yes() {

    }

    protected void no() {

    }
}