package com.gempukku.stccg.decisions;

import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

import java.util.List;

public abstract class MultipleChoiceAwaitingDecision extends AbstractAwaitingDecision {
    private final String[] _possibleResults;

    public MultipleChoiceAwaitingDecision(String text, String[] possibleResults) {
        this(text, possibleResults, -1);
    }
    public MultipleChoiceAwaitingDecision(String text, List<String> possibleResults) {
        this(text, possibleResults.toArray(new String[0]), -1);
    }
    public MultipleChoiceAwaitingDecision(String text, String[] possibleResults, int defaultIndex) {
        super(1, text, AwaitingDecisionType.MULTIPLE_CHOICE);
        _possibleResults = possibleResults;
        setParam("results", _possibleResults);
        setParam("defaultIndex", String.valueOf(defaultIndex)); // TODO SNAPSHOT - defaultIndex does not pass through to client
    }

    protected abstract void validDecisionMade(int index, String result);

    @Override
    public final void decisionMade(String result) throws DecisionResultInvalidException {
        if (result == null)
            throw new DecisionResultInvalidException();

        int index;
        try {
            index = Integer.parseInt(result);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException("Unknown response number");
        }
        validDecisionMade(index, _possibleResults[index]);
    }
}
