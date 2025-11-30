package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.modifiers.KillCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class KilledTriggerChecker implements TriggerChecker {

        private final FilterBlueprint _killedPersonnelFilterBlueprint;

        public KilledTriggerChecker(FilterBlueprint killedPersonnelFilterBlueprint) {
                _killedPersonnelFilterBlueprint = killedPersonnelFilterBlueprint;
        }

        @Override
        public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
            ActionResult actionResult = actionContext.getEffectResult();
            if (actionResult instanceof KillCardResult killCardResult) {
                    Filterable killedPersonnelFilter = _killedPersonnelFilterBlueprint.getFilterable(cardGame, actionContext);
                    PhysicalCard killedCard = killCardResult.getKilledCard();
                    return Filters.and(killedPersonnelFilter).accepts(cardGame, killedCard);
            } else {
                    return false;
            }
        }

}