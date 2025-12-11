package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.PlayPhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.List;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    protected TriggerActionBlueprint(int limitPerTurn,
                                     TriggerChecker triggerChecker, List<Requirement> requirements,
                                     List<SubActionBlueprint> costs, List<SubActionBlueprint> effects,
                                     boolean triggerDuringSeed, PlayerSource player)
            throws InvalidCardDefinitionException {
        super(limitPerTurn, costs, effects, player);
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

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, actionContext)) {
            UseGameTextAction action = new UseGameTextAction(cardGame, thisCard, actionContext);
            appendActionToContext(cardGame, action, actionContext);
            return action;
        }
        return null;
    }

}