package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

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