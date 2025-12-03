package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.PlayPhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.List;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    protected TriggerActionBlueprint(int limitPerTurn,
                                     TriggerChecker triggerChecker, List<Requirement> requirements,
                                     List<SubActionBlueprint> costs, List<SubActionBlueprint> effects,
                                     boolean triggerDuringSeed)
            throws InvalidCardDefinitionException {
        super(limitPerTurn, costs, effects);
        if (requirements != null) {
            _requirements.addAll(requirements);
        }
        if (triggerChecker != null) {
            _requirements.add(triggerChecker);
        }
        if (!triggerDuringSeed) {
            _requirements.add(new PlayPhaseRequirement());
        }
    }

    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard card) {
        cardGame.sendErrorMessage("Tried to create trigger action without a defined action result");
        return null;
    }

    public abstract TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                          PhysicalCard thisCard, ActionResult result);

}