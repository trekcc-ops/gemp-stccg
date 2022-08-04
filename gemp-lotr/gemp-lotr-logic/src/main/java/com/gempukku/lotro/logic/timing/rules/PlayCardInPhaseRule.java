package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.timing.Action;

import java.util.LinkedList;
import java.util.List;

public class PlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public PlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
                        final Phase phase = game.getGameState().getCurrentPhase();
                        Side side = GameUtils.getSide(game, playerId);
                        if (phase == Phase.FELLOWSHIP) {
                            if (GameUtils.isFP(game, playerId)) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.or(Filters.and(CardType.EVENT, Keyword.FELLOWSHIP), Filters.not(CardType.EVENT)))) {
                                    if (PlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        } else if (phase == Phase.SHADOW) {
                            if (GameUtils.isShadow(game, playerId)) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.or(Filters.and(CardType.EVENT, Keyword.SHADOW), Filters.not(CardType.EVENT)))) {
                                    if (PlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        } else {
                            final Keyword phaseKeyword = PlayUtils.PhaseKeywordMap.get(game.getGameState().getCurrentPhase());
                            if (phaseKeyword != null) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.and(CardType.EVENT, phaseKeyword))) {
                                    if (PlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        }

                        return null;
                    }
                }
        );
    }
}
