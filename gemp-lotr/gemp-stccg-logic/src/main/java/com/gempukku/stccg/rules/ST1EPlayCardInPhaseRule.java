package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;
    private final DefaultGame _game;

    public ST1EPlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<Action> getPhaseActions(String playerId) {
                        final Phase phase = _game.getGameState().getCurrentPhase();
                        if (phase == Phase.SEED_DOORWAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                                if (card.canBePlayed()) {
                                    result.add(new PlayPermanentForFreeAction(card, Zone.TABLE));
                                }
                            }
                            return result;
                        } else if (phase == Phase.SEED_MISSION && !_game.getGameState().getHand(playerId).isEmpty()) {
                            if (Objects.equals(playerId, _game.getGameState().getCurrentPlayerId())) {
                                List<Action> actionList = new LinkedList<>();
                                actionList.add(new SeedMissionAction((PhysicalMissionCard) _game.getGameState().getHand(playerId).get(0)));
                                return actionList;
                            }
                        } else if (phase == Phase.SEED_FACILITY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                                if (Objects.equals(playerId, _game.getGameState().getCurrentPlayerId())) {
                                    if (card.canBePlayed()) {
                                        if (card.getBlueprint().getFacilityType() == FacilityType.OUTPOST)
                                            result.add(((PhysicalFacilityCard) card).createSeedCardAction());
                                    }
                                }
                            }
                            return result;
                        } else if (phase == Phase.CARD_PLAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                                if (Objects.equals(playerId, _game.getGameState().getCurrentPlayerId())) {
                                    if (card.canBePlayed()) {
                                        if (card.getCardType() == CardType.PERSONNEL || card.getCardType() == CardType.SHIP)
                                            result.add(((PhysicalReportableCard1E) card).createReportCardAction());
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