package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.decisions.responses.MultipleChoiceDecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;

import java.util.Collection;

public abstract class MultipleChoiceAwaitingDecision extends AbstractAwaitingDecision {

    @JsonProperty("options")
    private final String[] _possibleResults;

    @JsonProperty("context")
    private final DecisionContext _context;

    private boolean _responded;

    protected String _selectedValue;
    protected int _selectedIndex;

    public MultipleChoiceAwaitingDecision(Player player, String text, String[] possibleResults,
                                          DefaultGame cardGame) {
        super(player, text, cardGame);
        _possibleResults = possibleResults;
        _context = DecisionContext.GENERAL_MULTIPLE_CHOICE;
    }

    public MultipleChoiceAwaitingDecision(Player player, DecisionContext context, String[] possibleResults,
                                          DefaultGame cardGame) {
        super(player, context, cardGame);
        _possibleResults = possibleResults;
        _context = context;
    }




    public MultipleChoiceAwaitingDecision(Player player, String text, Collection<String> possibleResults,
                                          DefaultGame cardGame) {
        this(player, text, possibleResults.toArray(new String[0]), cardGame);
    }

    public MultipleChoiceAwaitingDecision(Player player, Collection<String> possibleResults,
                                          DefaultGame cardGame, DecisionContext context) {
        super(player, context, cardGame);
        _possibleResults = possibleResults.toArray(new String[0]);
        _context = context;
    }

    public void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException {
        if (_responded) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        } else if (response instanceof MultipleChoiceDecisionResponse choiceResponse) {
            int responseIndex = choiceResponse.getResponseIndex();
            if (responseIndex >= _possibleResults.length) {
                throw new DecisionResultInvalidException("Received an invalid response for decision");
            } else {
                _selectedIndex = responseIndex;
                _selectedValue = _possibleResults[responseIndex];
                _responded = true;
            }
        }
    }

    public void setDecisionResponse(int responseIndex) throws DecisionResultInvalidException {
        if (_responded) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        }
        if (responseIndex >= _possibleResults.length) {
            throw new DecisionResultInvalidException("Received an invalid response for decision");
        } else {
            _selectedIndex = responseIndex;
            _selectedValue = _possibleResults[responseIndex];
            _responded = true;
        }
    }

    public void setResponseAndFollowUp(int responseIndex) throws DecisionResultInvalidException, InvalidGameOperationException {
        setDecisionResponse(responseIndex);
        try {
            followUp();
        } catch(InvalidGameLogicException exp) {
            throw new InvalidGameOperationException(exp.getMessage());
        }
    }

    public String getElementType() { return "STRING"; }
}