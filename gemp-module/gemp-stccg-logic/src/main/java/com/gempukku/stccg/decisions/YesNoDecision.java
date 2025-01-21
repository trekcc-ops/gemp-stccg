package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class YesNoDecision extends MultipleChoiceAwaitingDecision {

    public YesNoDecision(Player player, String text, DefaultGame cardGame) {
        super(player, text, new String[]{"Yes", "No"}, cardGame);
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