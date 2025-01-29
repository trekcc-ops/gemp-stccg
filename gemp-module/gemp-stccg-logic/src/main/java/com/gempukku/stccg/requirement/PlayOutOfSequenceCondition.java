package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.TribblesActionContext;

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
            _value = Integer.valueOf(pieces[1].trim());
        }
    }

    public boolean accepts(ActionContext actionContext) {
        TribblesActionContext context = (TribblesActionContext) actionContext;
        return switch(_valueCheck) {
            case lastTribblePlayed -> context.getGameState().getLastTribblePlayed() == _value;
            case nextTribbleInSequence -> context.getGameState().getNextTribbleInSequence() == _value;
            case tribbleSequenceBroken -> context.getGameState().isChainBroken();
        };
    }
}