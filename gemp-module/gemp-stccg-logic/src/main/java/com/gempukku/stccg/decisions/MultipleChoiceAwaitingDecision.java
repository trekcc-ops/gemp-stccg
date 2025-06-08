package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.Collection;

public abstract class MultipleChoiceAwaitingDecision extends AbstractAwaitingDecision {

    @JsonProperty("results")
    private final String[] _possibleResults;

    @JsonProperty("context")
    private final DecisionContext _context;

    public MultipleChoiceAwaitingDecision(Player player, String text, String[] possibleResults,
                                          DefaultGame cardGame) {
        super(player, text, AwaitingDecisionType.MULTIPLE_CHOICE, cardGame);
        _possibleResults = possibleResults;
        _context = DecisionContext.OTHER;
    }

    public MultipleChoiceAwaitingDecision(Player player, DecisionContext context, String[] possibleResults,
                                          DefaultGame cardGame) {
        super(player, context, AwaitingDecisionType.MULTIPLE_CHOICE, cardGame);
        _possibleResults = possibleResults;
        _context = context;
    }




    public MultipleChoiceAwaitingDecision(Player player, String text, Collection<String> possibleResults,
                                          DefaultGame cardGame) {
        this(player, text, possibleResults.toArray(new String[0]), cardGame);
    }

    public MultipleChoiceAwaitingDecision(Player player, Collection<String> possibleResults,
                                          DefaultGame cardGame, DecisionContext context) {
        super(player, context, AwaitingDecisionType.MULTIPLE_CHOICE, cardGame);
        _possibleResults = possibleResults.toArray(new String[0]);
        _context = context;
    }



    protected abstract void validDecisionMade(int index, String result)
            throws InvalidGameLogicException, DecisionResultInvalidException;

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
        try {
            validDecisionMade(index, _possibleResults[index]);
        } catch(InvalidGameLogicException exp) {
            throw new DecisionResultInvalidException(exp.getMessage());
        }
    }

    public String[] getCardIds() {
        return null;
    }
}