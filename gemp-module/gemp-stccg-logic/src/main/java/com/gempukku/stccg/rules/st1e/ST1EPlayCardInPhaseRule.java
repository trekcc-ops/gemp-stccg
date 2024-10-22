package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule extends ST1ERule {

    public ST1EPlayCardInPhaseRule(ST1EGame game) {
        super(game);
    }

    @Override
    public List<Action> getPhaseActions(String playerId) {
        final List<PhysicalCard> cardsInHand = _game.getGameState().getHand(playerId);
        final String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        final List<Action> result = new LinkedList<>();

        final Phase phase = _game.getGameState().getCurrentPhase();
        if (phase == Phase.SEED_DOORWAY) {
            for (PhysicalCard card : cardsInHand) {
                Action action = card.getPlayCardAction();
                if (action.canBeInitiated())
                    result.add(action);
            }
            return result;
        } else if (phase == Phase.SEED_MISSION && !cardsInHand.isEmpty()) {
            if (Objects.equals(playerId, currentPlayerId)) {
                result.add(new SeedMissionCardAction((MissionCard) cardsInHand.getFirst()));
            }
        } else if (phase == Phase.SEED_FACILITY) {
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
        }
        return result;
    }
}