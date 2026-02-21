package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public abstract class YesNoDecision extends MultipleChoiceAwaitingDecision {

    public YesNoDecision(Player player, DecisionContext context, DefaultGame cardGame) {
        super(player, context, new String[]{"Yes", "No"}, cardGame);
    }

    public YesNoDecision(String playerName, String text, DefaultGame cardGame) {
        super(playerName, text, new String[]{"Yes", "No"}, cardGame);
    }



    @Override
    protected final void validDecisionMade(int index, String result) {
        if (index == 0)
            yes();
        else
            no();
    }

    protected abstract void yes();

    protected abstract void no();
}