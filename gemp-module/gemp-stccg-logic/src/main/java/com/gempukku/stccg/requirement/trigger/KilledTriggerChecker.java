package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.modifiers.KillCardResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class KilledTriggerChecker implements TriggerChecker {

        private final FilterBlueprint _killedPersonnelFilterBlueprint;

        public KilledTriggerChecker(FilterBlueprint killedPersonnelFilterBlueprint) {
                _killedPersonnelFilterBlueprint = killedPersonnelFilterBlueprint;
        }

        @Override
        public boolean accepts(ActionContext actionContext) {
            ActionResult actionResult = actionContext.getEffectResult();
            if (actionResult instanceof KillCardResult killCardResult) {
                    Filterable killedPersonnelFilter = _killedPersonnelFilterBlueprint.getFilterable(actionContext);
                    PhysicalCard killedCard = killCardResult.getKilledCard();
                    return Filters.and(killedPersonnelFilter).accepts(actionContext.getGame(), killedCard);
            } else {
                    return false;
            }
        }

}