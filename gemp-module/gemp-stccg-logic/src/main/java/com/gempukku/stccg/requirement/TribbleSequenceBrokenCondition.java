package com.gempukku.stccg.requirement;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;

public class TribbleSequenceBrokenCondition implements Condition {

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        if (cardGame instanceof TribblesGame tribblesGame) {
            return tribblesGame.getGameState().isChainBroken();
        } else {
            return false;
        }
    }
}