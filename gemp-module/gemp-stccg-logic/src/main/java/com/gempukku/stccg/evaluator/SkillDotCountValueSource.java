package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class SkillDotCountValueSource extends ValueSource {

    private final CardTargetBlueprint _personnelBlueprint;

    public SkillDotCountValueSource(
            @JsonProperty(value = "personnel", required = true)
            CardTargetBlueprint personnelBlueprint) {
        _personnelBlueprint = personnelBlueprint;
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        ActionCardResolver resolver = _personnelBlueprint.getTargetResolver(cardGame, actionContext);
        Collection<PhysicalCard> cards = resolver.getCards(cardGame);
        int result = 0;
        for (PhysicalCard card : cards) {
            if (card instanceof PersonnelCard personnel) {
                result = result + personnel.getSkillDotCount();
            }
        }
        return result;
    }
}