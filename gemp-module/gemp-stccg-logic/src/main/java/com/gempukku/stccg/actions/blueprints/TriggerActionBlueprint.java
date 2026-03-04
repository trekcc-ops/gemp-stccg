package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerSource;
import com.gempukku.stccg.requirement.PlayPhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    protected TriggerActionBlueprint(Requirement triggerChecker, List<Requirement> requirements,
                                     List<SubActionBlueprint> costs, List<SubActionBlueprint> effects,
                                     boolean triggerDuringSeed, PlayerSource player) {
        super(costs, effects, player);
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
    public UseGameTextAction createAction(DefaultGame cardGame, GameTextContext context) {
        if (context.acceptsAllRequirements(cardGame, _requirements)) {
            UseGameTextAction action = new UseGameTextAction(cardGame, context.card(), context);
            appendSubActions(action);
            return action;
        }
        return null;
    }

}