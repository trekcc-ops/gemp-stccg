package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class SkillDotCountEvaluator extends Evaluator {
    private final SelectVisibleCardAction _selectAction;

    public SkillDotCountEvaluator(SelectVisibleCardAction selectAction, DefaultGame cardGame) {
        super(cardGame);
        _selectAction = selectAction;
    }

    public int evaluateExpression(DefaultGame cardGame) {
        try {
            if (_selectAction != null && _selectAction.wasCarriedOut()) {
                PhysicalCard selectedCard = _selectAction.getSelectedCard();
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

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return evaluateExpression(game);
    }
}