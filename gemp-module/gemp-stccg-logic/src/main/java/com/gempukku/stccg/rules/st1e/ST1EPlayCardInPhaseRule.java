package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.playcard.SeedMissionAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule {
    private final ActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public ST1EPlayCardInPhaseRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<Action> getPhaseActions(String playerId) {
                        final List<PhysicalCard> cardsInHand = _game.getGameState().getHand(playerId);
                        final Player player = _game.getGameState().getPlayer(playerId);
                        final String currentPlayerId = _game.getGameState().getCurrentPlayerId();

                        final Phase phase = _game.getGameState().getCurrentPhase();
                        if (phase == Phase.SEED_DOORWAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : cardsInHand) {
                                Action action = card.getPlayCardAction();
                                if (action.canBeInitiated())
                                    result.add(action);
                            }
                            return result;
                        } else if (phase == Phase.SEED_MISSION && !cardsInHand.isEmpty()) {
                            if (Objects.equals(playerId, currentPlayerId)) {
                                List<Action> actionList = new LinkedList<>();
                                actionList.add(new SeedMissionAction((MissionCard) cardsInHand.get(0)));
                                return actionList;
                            }
                        } else if (phase == Phase.SEED_FACILITY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : cardsInHand) {
                                if (Objects.equals(playerId, currentPlayerId)) {
                                    if (card.canBeSeeded()) {
                                        Action action = card.createSeedCardAction();
                                        if (action != null && action.canBeInitiated())
                                            result.add(action);
                                    }
                                }
                            }
                            return result;
                        } else if (phase == Phase.CARD_PLAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                                if (Objects.equals(playerId, _game.getGameState().getCurrentPlayerId())) {
                                    if (card.canBePlayed()) {
                                        if (card instanceof PhysicalReportableCard1E reportable) {
                                            Action action = reportable.createReportCardAction();
                                            if (action != null && action.canBeInitiated())
                                                result.add(action);
                                        }
                                    }
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