package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    private static final Map<Phase, Keyword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Keyword.FELLOWSHIP);
        put(Phase.SHADOW, Keyword.SHADOW);
        put(Phase.MANEUVER, Keyword.MANEUVER);
        put(Phase.ARCHERY, Keyword.ARCHERY);
        put(Phase.ASSIGNMENT, Keyword.ASSIGNMENT);
        put(Phase.SKIRMISH, Keyword.SKIRMISH);
        put(Phase.REGROUP, Keyword.REGROUP);
    }});

    public PlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        final Keyword phaseKeyword = PhaseKeywordMap.get(actionsEnvironment.getGame().getGameState().getCurrentPhase());
                        if (phaseKeyword != null) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(actionsEnvironment.getGame().getGameState().getHand(playerId), actionsEnvironment.getGame(),
                                    Filters.and(CardType.EVENT, phaseKeyword))) {
                                if (actionsEnvironment.getGame().checkPlayRequirements(card))
                                    result.add(card.getPlayCardAction(0, Filters.any, false));
                            }
                            return result;
                        }
                        return null;
                    }
                }
        );
    }
}
