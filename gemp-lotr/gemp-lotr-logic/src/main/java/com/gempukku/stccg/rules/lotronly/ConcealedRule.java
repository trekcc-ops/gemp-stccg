package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.RemoveTwilightEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.evaluator.LocationEvaluator;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.requirement.trigger.TriggerConditions;

import java.util.*;

public class ConcealedRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public ConcealedRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
            new AbstractActionProxy() {
                @Override
                public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(final DefaultGame game, EffectResult effectResult) {
                    if (TriggerConditions.moves(effectResult)
                            && game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
                        List<RequiredTriggerAction> actions = new ArrayList<>();

                        int twilight = Filters.filterActive(game, CardType.COMPANION, Keyword.CONCEALED).size();

                        if(twilight == 0)
                            return null;

                        final RequiredTriggerAction action = new RequiredTriggerAction(null);
                        LocationEvaluator loc = new LocationEvaluator(twilight, 0, Keyword.EXPOSED);
                        action.appendEffect(new RemoveTwilightEffect(new LocationEvaluator(twilight, 0, Keyword.EXPOSED)));

                        if(loc.evaluateExpression(game, null) == 0) {
                            action.setText("Concealed companions were exposed!  No twilight was removed.");
                        }
                        else {
                            action.setText("Concealed companions trigger removal of " + twilight + " twilight.");
                        }
                        actions.add(action);

                        return actions;
                    }
                    return null;
                }
            });
    }
}
