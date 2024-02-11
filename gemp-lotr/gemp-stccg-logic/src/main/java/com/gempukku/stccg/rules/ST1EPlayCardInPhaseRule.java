package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.cards.PhysicalPersonnelCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;

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
                    public List<Action> getPhaseActions(String playerId, ST1EGame game) {
                        final Phase phase = game.getGameState().getCurrentPhase();
                        if (phase == Phase.SEED_DOORWAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game)) {
                                if (game.checkPlayRequirements(card)) {
                                    result.add(new PlayPermanentForFreeAction(card, Zone.TABLE));
                                }
                            }
                            return result;
                        } else if (phase == Phase.SEED_MISSION && !game.getGameState().getHand(playerId).isEmpty()) {
                            if (Objects.equals(playerId, game.getGameState().getCurrentPlayerId())) {
                                List<Action> actionList = new LinkedList<>();
                                actionList.add(new SeedMissionAction(game, (PhysicalMissionCard) game.getGameState().getHand(playerId).get(0)));
                                return actionList;
                            }
                        } else if (phase == Phase.SEED_FACILITY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game)) {
                                if (Objects.equals(playerId, game.getGameState().getCurrentPlayerId())) {
                                    if (game.checkPlayRequirements(card)) {
                                        if (card.getBlueprint().getFacilityType() == FacilityType.OUTPOST)
                                            result.add(new SeedOutpostAction(game, (PhysicalFacilityCard) card));
                                    }
                                }
                            }
                            return result;
                        } else if (phase == Phase.NORMAL_CARD_PLAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game)) {
                                if (Objects.equals(playerId, game.getGameState().getCurrentPlayerId())) {
                                    if (game.checkPlayRequirements(card)) {
                                        if (card.getBlueprint().getCardType() == CardType.PERSONNEL || card.getBlueprint().getCardType() == CardType.SHIP)
                                            result.add(new ReportCardAction(game, (PhysicalPersonnelCard) card));
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