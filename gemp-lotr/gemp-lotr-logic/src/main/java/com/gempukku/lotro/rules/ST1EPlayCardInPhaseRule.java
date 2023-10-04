package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.actions.PlayMissionAction;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ST1EPlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy<ST1EGame>() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, ST1EGame game) {
                        final Phase phase = game.getGameState().getCurrentPhase();
                        if (phase == Phase.SEED_MISSION && game.getGameState().getMissionPile(playerId).size() > 0) {
                            if (Objects.equals(playerId, game.getGameState().getCurrentPlayerId())) {
                                List<Action> actionList = new LinkedList<>();
                                actionList.add(new PlayMissionAction(game.getGameState().getTopOfMissionPile(playerId)));
                                return actionList;
                            }
                        }
                        return null;
                    }
                }
        );
    }
}
