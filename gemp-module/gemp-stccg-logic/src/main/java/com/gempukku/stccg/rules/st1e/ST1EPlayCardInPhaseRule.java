package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule extends ST1ERule {

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        final List<TopLevelSelectableAction> result = new LinkedList<>();
        if (cardGame instanceof ST1EGame stGame) {
            final List<PhysicalCard> cardsInHand = player.getCardsInHand();
            final String currentPlayerId = cardGame.getGameState().getCurrentPlayerId();
            boolean isCurrentPlayer = Objects.equals(player.getPlayerId(), currentPlayerId);

            for (PhysicalCard card : cardsInHand) {
                for (TopLevelSelectableAction action : card.getPlayActionsFromGameText(player, cardGame)) {
                    if (action != null && action.canBeInitiated(cardGame)) {
                        result.add(action);
                    }
                }
            }

            final Phase phase = cardGame.getGameState().getCurrentPhase();
            if (phase == Phase.SEED_DOORWAY) {
                for (PhysicalCard card : cardsInHand) {
                    ST1EPhysicalCard stCard = (ST1EPhysicalCard) card;
                    for (TopLevelSelectableAction action : stCard.createSeedCardActions(cardGame)) {
                        if (action != null && action.canBeInitiated(cardGame)) {
                            result.add(action);
                        }
                    }
                }
                return result;
            } else if (phase == Phase.SEED_MISSION && !player.getMissionsPile().isEmpty() && isCurrentPlayer) {
                CardPile<PhysicalCard> missionsPile = player.getMissionsPile();
                if (missionsPile.getTopCard() instanceof MissionCard missionCard) {
                    result.add(new SeedMissionCardAction(cardGame, missionCard));
                }
            } else if (phase == Phase.SEED_FACILITY) {
                for (PhysicalCard card : player.getCardsInGroup(Zone.SEED_DECK)) {
                    if (isCurrentPlayer) {
                        if (canCardBeSeeded(card, stGame)) {
                            ST1EPhysicalCard stCard = (ST1EPhysicalCard) card;
                            for (TopLevelSelectableAction action : stCard.createSeedCardActions(cardGame)) {
                                if (action != null && action.canBeInitiated(cardGame)) {
                                    result.add(action);
                                }
                            }
                        }
                    }
                }
                return result;
            } else if (phase == Phase.CARD_PLAY) {
                for (PhysicalCard card : Filters.filter(player.getCardsInHand(), cardGame)) {
                    if (isCurrentPlayer) {
                        if (cardGame.getRules()
                                .cardCanEnterPlay(cardGame, card, PlayCardAction.EnterPlayActionType.PLAY)) {
                            TopLevelSelectableAction action = card.getPlayCardAction(cardGame);
                            if (action != null && action.canBeInitiated(cardGame))
                                result.add(action);
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean canCardBeSeeded(PhysicalCard card, ST1EGame game) {
        return switch(card) {
            case FacilityCard facility -> canFacilityBeSeeded(facility, game);
            case MissionCard ignored -> true;
            default -> game.getRules().cardCanEnterPlay(game, card, PlayCardAction.EnterPlayActionType.SEED);
        };
    }

    public boolean canFacilityBeSeeded(FacilityCard facility, ST1EGame game) {
        for (MissionLocation location : game.getGameState().getSpacelineLocations()) {
            boolean canSeedHere = game.getRules().isLocationValidPlayCardDestinationPerRules(
                    game, facility, location, SeedCardAction.class, facility.getOwnerName(),
                    facility.getAffiliationOptions());
            if (canSeedHere)
                return true;
        }
        return false;
    }


}