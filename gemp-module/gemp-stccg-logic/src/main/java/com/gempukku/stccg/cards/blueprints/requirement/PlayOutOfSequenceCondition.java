package com.gempukku.stccg.cards.blueprints.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.TribblesActionContext;

public class PlayOutOfSequenceCondition implements Requirement {

    private enum ValueCheck { nextTribbleInSequence, lastTribblePlayed, tribbleSequenceBroken }

    private final ValueCheck _valueCheck;
    private final int _value;

    public PlayOutOfSequenceCondition(String jsonString) {
        if (jsonString.equals("tribbleSequenceBroken")) {
            _valueCheck = ValueCheck.tribbleSequenceBroken;
            _value = 1;
        } else {
            String[] pieces = jsonString.split("=");
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