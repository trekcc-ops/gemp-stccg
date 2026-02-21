package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.discard.KillCardResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class KilledTriggerChecker implements TriggerChecker {

    private final FilterBlueprint _killedPersonnelFilterBlueprint;

    @JsonCreator
    public KilledTriggerChecker(@JsonProperty("filter")
            FilterBlueprint killedPersonnelFilterBlueprint) {
        _killedPersonnelFilterBlueprint = killedPersonnelFilterBlueprint;
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        ActionResult actionResult = cardGame.getCurrentActionResult();
        if (actionResult instanceof KillCardResult killCardResult) {
            Filterable killedPersonnelFilter = _killedPersonnelFilterBlueprint.getFilterable(cardGame, actionContext);
            PhysicalCard killedCard = killCardResult.getKilledCard();
            return Filters.and(killedPersonnelFilter).accepts(cardGame, killedCard);
        } else {
                return false;
        }
    }
}