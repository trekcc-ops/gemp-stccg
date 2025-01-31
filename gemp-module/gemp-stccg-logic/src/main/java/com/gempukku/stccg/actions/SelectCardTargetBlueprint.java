package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;

public class SelectCardTargetBlueprint implements CardTargetBlueprint {

    private final FilterBlueprint _filterBlueprint;
    private final int _count;
    private final boolean _randomSelection;

    SelectCardTargetBlueprint(@JsonProperty("filter")
                              FilterBlueprint filterBlueprint,
                              @JsonProperty("count")
                              int count,
                              @JsonProperty("random")
                              boolean randomSelection) {
        _filterBlueprint = filterBlueprint;
        _count = count;
        _randomSelection = randomSelection;
    }

    public ActionCardResolver getTargetResolver(ActionContext context) {
        Collection<PhysicalCard> selectableCards =
                Filters.filter(context.getGame().getGameState().getAllCardsInGame(),
                        _filterBlueprint.getFilterable(context));
        SelectCardsAction selectAction = new SelectRandomCardAction(
                context.getGame(), context.getPerformingPlayer(), "Select a card", selectableCards);
        return new SelectCardsResolver(selectAction);
    }

}