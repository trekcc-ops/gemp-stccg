package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class SkillDotCountEvaluator extends Evaluator {

    @JsonProperty("selectActionId")
    private int _selectActionId;

    public SkillDotCountEvaluator(SelectCardAction selectAction) {
        _selectActionId = selectAction.getActionId();
    }

    public float evaluateExpression(DefaultGame cardGame) {
        try {
            Action action = cardGame.getActionById(_selectActionId);
            if (action instanceof SelectCardAction selectAction && selectAction.wasCarriedOut()) {
                PhysicalCard selectedCard = selectAction.getSelectedCard();
                if (selectedCard instanceof PersonnelCard personnel) {
                    return personnel.getSkillDotCount();
                }
            }
            throw new InvalidGameLogicException("Unable to evaluate skill dot count expression");
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return 0;
        }
    }
}