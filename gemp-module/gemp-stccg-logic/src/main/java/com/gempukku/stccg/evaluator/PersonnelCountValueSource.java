package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class PersonnelCountValueSource implements SingleValueSource {

    private final FilterBlueprint _filter;

    private PersonnelCountValueSource(@JsonProperty("filter")FilterBlueprint filter) {
        _filter = filter;
    }
    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        CardFilter cardFilter = _filter.getFilterable(cardGame, context);
        Collection<PhysicalCard> cards = Filters.filter(cardGame, cardFilter);
        return new Evaluator() {
            @Override
            public float evaluateExpression(DefaultGame cardGame) {
                int total = 0;
                for (PhysicalCard card : cards) {
                    if (card instanceof PersonnelCard) {
                        total++;
                    }
                }
                return total;
            }
        };
    }
}