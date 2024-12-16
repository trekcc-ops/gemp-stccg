package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SkillDotCountEvaluator extends Evaluator {
    private final SelectCardInPlayAction _selectAction;

    public SkillDotCountEvaluator(SelectCardInPlayAction selectAction, DefaultGame cardGame) {
        super(cardGame);
        _selectAction = selectAction;
    }

    public int evaluateExpression(DefaultGame cardGame) {
        if (_selectAction != null && _selectAction.wasCarriedOut()) {
            PhysicalCard selectedCard = _selectAction.getSelectedCard();
            if (selectedCard instanceof PersonnelCard personnel) {
                return personnel.getSkillDotCount();
            }
        }
        cardGame.sendMessage("ERROR: Unable to evaluate skill dot count expression");
        return 0;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        if (_selectAction != null && _selectAction.wasCarriedOut()) {
            PhysicalCard selectedCard = _selectAction.getSelectedCard();
            if (selectedCard != null && selectedCard instanceof PersonnelCard personnel) {
                return personnel.getSkillDotCount();
            }
        }
        game.sendMessage("ERROR: Unable to evaluate skill dot count expression");
        return 0;
    }
}