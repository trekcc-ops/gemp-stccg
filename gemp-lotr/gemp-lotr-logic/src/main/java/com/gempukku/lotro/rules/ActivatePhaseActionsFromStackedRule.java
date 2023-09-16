package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.rules.lotronly.LotroGameUtils;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromStackedRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsFromStackedRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        List<Action> result = new LinkedList<>();
                        final Side side = LotroGameUtils.getSide(game, playerId);
                        for (LotroPhysicalCard activatableCard : Filters.filter(game.getGameState().getStacked(playerId), game, side,
                                Filters.stackedOn(Filters.active))) {
                            List<? extends Action> list = activatableCard.getBlueprint().getPhaseActionsFromStacked(playerId, game, activatableCard);
                            if (list != null)
                                result.addAll(list);

                            final List<? extends Action> extraActions = game.getModifiersQuerying().getExtraPhaseActionsFromStacked(game, activatableCard);
                            if (extraActions != null) {
                                for (Action action : extraActions) {
                                    if (action != null)
                                        result.add(action);
                                }
                            }
                        }
                        return result;
                    }
                });
    }
}
