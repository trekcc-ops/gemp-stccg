package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.CardWithStrength;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class IntegrityValueSource implements SingleValueSource {

    private final FilterBlueprint _filterBlueprint;

    @JsonCreator
    private IntegrityValueSource(@JsonProperty("card")FilterBlueprint filterBlueprint) {
        _filterBlueprint = filterBlueprint;
    }
    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        CardFilter filter = _filterBlueprint.getFilterable(cardGame, context);
        return new Evaluator() {
            @Override
            public float evaluateExpression(DefaultGame cardGame) {
                int result = 0;
                for (PhysicalCard card : Filters.filter(cardGame, filter)) {
                    if (card instanceof CardWithStrength cardWithStrength) {
                        result = result + cardWithStrength.getIntegrity(cardGame);
                    }
                }
                return result;
            }
        };
    }
}