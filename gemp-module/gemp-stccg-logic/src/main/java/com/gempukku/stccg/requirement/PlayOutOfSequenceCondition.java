package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class PlayOutOfSequenceCondition implements Requirement {

    private enum ValueCheck { nextTribbleInSequence, lastTribblePlayed, tribbleSequenceBroken }

    private final ValueCheck _valueCheck;
    private final int _value;

    public PlayOutOfSequenceCondition(@JsonProperty(value = "condition", required = true)
                                      String condition) {
        if (condition.equals("tribbleSequenceBroken")) {
            _valueCheck = ValueCheck.tribbleSequenceBroken;
            _value = 1;
        } else {
            String[] pieces = condition.split("=");
            _valueCheck = ValueCheck.valueOf(pieces[0].trim());
            _value = Integer.parseInt(pieces[1].trim());
        }
    }

    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        if (cardGame instanceof TribblesGame tribblesGame) {
            TribblesGameState gameState = tribblesGame.getGameState();
            return switch (_valueCheck) {
                case lastTribblePlayed -> gameState.getLastTribblePlayed() == _value;
                case nextTribbleInSequence -> gameState.getNextTribbleInSequence() == _value;
                case tribbleSequenceBroken -> gameState.isChainBroken();
            };
        } else {
            return false;
        }
    }
}