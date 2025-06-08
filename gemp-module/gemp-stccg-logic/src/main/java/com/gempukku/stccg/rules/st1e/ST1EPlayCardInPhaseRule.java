package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.cardgroup.MissionCardPile;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule extends ST1ERule {

    public ST1EPlayCardInPhaseRule(ST1EGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        final List<PhysicalCard> cardsInHand = player.getCardsInHand();
        final String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        final List<TopLevelSelectableAction> result = new LinkedList<>();
        boolean isCurrentPlayer = Objects.equals(player.getPlayerId(), currentPlayerId);

        for (PhysicalCard card : cardsInHand) {
            for (TopLevelSelectableAction action : card.getPlayActionsFromGameText(player, _game)) {
                if (action != null && action.canBeInitiated(_game)) {
                    result.add(action);
                }
            }
        }

        final Phase phase = _game.getGameState().getCurrentPhase();
        if (phase == Phase.SEED_DOORWAY) {
            for (PhysicalCard card : cardsInHand) {
                ST1EPhysicalCard stCard = (ST1EPhysicalCard) card;
                for (TopLevelSelectableAction action : stCard.createSeedCardActions()) {
                    if (action != null && action.canBeInitiated(_game)) {
                        result.add(action);
                    }
                }
            }
            return result;
        } else if (phase == Phase.SEED_MISSION && !player.getMissionsPile().isEmpty() && isCurrentPlayer) {
            CardPile missionsPile = player.getMissionsPile();
            result.add(new SeedMissionCardAction((MissionCard) missionsPile.getTopCard()));
        } else if (phase == Phase.SEED_FACILITY) {
            for (PhysicalCard card : player.getCardsInGroup(Zone.SEED_DECK)) {
                if (isCurrentPlayer) {
                    if (card.canBeSeeded(_game)) {
                        ST1EPhysicalCard stCard = (ST1EPhysicalCard) card;
                        for (TopLevelSelectableAction action : stCard.createSeedCardActions()) {
                            if (action != null && action.canBeInitiated(_game)) {
                                result.add(action);
                            }
                        }
                    }
                }
            }
            return result;
        } else if (phase == Phase.CARD_PLAY) {
            for (PhysicalCard card : Filters.filter(player.getCardsInHand(), _game)) {
                if (isCurrentPlayer) {
                    if (card.canBePlayed(_game)) {
                        TopLevelSelectableAction action = card.getPlayCardAction();
                        if (action != null && action.canBeInitiated(_game))
                            result.add(action);
                    }
                }
            }
        }
        return result;
    }

}