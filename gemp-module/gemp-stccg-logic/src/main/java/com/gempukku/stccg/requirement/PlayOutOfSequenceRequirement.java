package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class PlayOutOfSequenceRequirement implements Requirement {

    private enum ValueCheck { nextTribbleInSequence, lastTribblePlayed, tribbleSequenceBroken }

    private final ValueCheck _valueCheck;

    private final int _value;

    public PlayOutOfSequenceRequirement(@JsonProperty(value = "condition", required = true)
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

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
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

    @Override
    public Condition getCondition(GameTextContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return switch (_valueCheck) {
            case lastTribblePlayed -> new LastTribblePlayedCondition(_value);
            case nextTribbleInSequence -> new NextTribbleInSequenceCondition(_value);
            case tribbleSequenceBroken -> new TribbleSequenceBrokenCondition();
        };
    }

}