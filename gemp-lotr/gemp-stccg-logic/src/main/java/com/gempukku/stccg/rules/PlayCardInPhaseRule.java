package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayCardInPhaseRule {
    private final ActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    private static final Map<Phase, Keyword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Keyword.FELLOWSHIP);
        put(Phase.SHADOW, Keyword.SHADOW);
        put(Phase.MANEUVER, Keyword.MANEUVER);
        put(Phase.ARCHERY, Keyword.ARCHERY);
        put(Phase.ASSIGNMENT, Keyword.ASSIGNMENT);
        put(Phase.SKIRMISH, Keyword.SKIRMISH);
        put(Phase.REGROUP, Keyword.REGROUP);
    }});

    public PlayCardInPhaseRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        final Keyword phaseKeyword = PhaseKeywordMap.get(_game.getGameState().getCurrentPhase());
                        if (phaseKeyword != null) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game,
                                    Filters.and(CardType.EVENT, phaseKeyword))) {
                                if (card.canBePlayed()) {
                                    Action action =
                                            card.getPlayCardAction(0, Filters.any, false);
                                    if (action.canBeInitiated())
                                        result.add(action);
                                }
                            }
                            return result;
                        }
                        return null;
                    }
                }
        );
    }
}
