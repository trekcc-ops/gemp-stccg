package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.AssignmentResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AmbushRule {
    private DefaultActionsEnvironment _actionsEnvironment;

    public AmbushRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ASSIGNMENT) {
                            AssignmentResult assignmentResult = (AssignmentResult) effectResult;
                            if (assignmentResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                                List<OptionalTriggerAction> actions = new LinkedList<OptionalTriggerAction>();
                                for (Set<PhysicalCard> minions : assignmentResult.getAssignments().values()) {
                                    for (PhysicalCard minion : minions) {
                                        if (game.getModifiersQuerying().hasKeyword(game.getGameState(), minion, Keyword.AMBUSH) && minion.getOwner().equals(playerId)) {
                                            final int count = game.getModifiersQuerying().getKeywordCount(game.getGameState(), minion, Keyword.AMBUSH);
                                            OptionalTriggerAction action = new OptionalTriggerAction(minion);
                                            action.setText("Ambush - add " + count);
                                            action.appendEffect(
                                                    new AddTwilightEffect(minion, count));
                                            actions.add(action);
                                        }
                                    }
                                }
                                return actions;
                            }
                        }
                        return null;
                    }
                });
    }
}
