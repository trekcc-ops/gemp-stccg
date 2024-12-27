package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.playcard.DownloadMultipleCardsToSameCompatibleOutpostAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.blueprints.actionsource.SeedCardActionSource;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class Blueprint109_063 extends CardBlueprint {

    // Assign Mission Specialists
    Blueprint109_063() {
        super("109_063");
    }

    @Override
    public SeedCardActionSource getSeedCardActionSource() {
        SeedCardActionSource actionSource = new SeedCardActionSource();
        actionSource.setSeedZone(Zone.TABLE);
        return actionSource;
    }

    public List<Action> getValidResponses(PhysicalCard thisCard, Player player, ActionResult actionResult) {
        DefaultGame game = player.getGame();
        List<Action> actions = new ArrayList<>();
        if (actionResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard &&
                thisCard.isControlledBy(player)) {

            List<FacilityCard> yourOutposts = new LinkedList<>();
            for (PhysicalCard card : Filters.yourFacilitiesInPlay(player)) {
                if (card instanceof FacilityCard facilityCard && facilityCard.getFacilityType() == FacilityType.OUTPOST)
                    yourOutposts.add(facilityCard);
            }

            List<PersonnelCard> specialistsNotInPlay = new LinkedList<>();
            for (PhysicalCard card : game.getGameState().getDrawDeck(player.getPlayerId())) {
                Collection<PhysicalCard> ownedCopiesInPlay = Filters.filterCardsInPlay(game,
                        Filters.copyOfCard(card), Filters.owner(player.getPlayerId()));
                if (card instanceof PersonnelCard personnel && personnel.getSkills().size() == 1 &&
                        personnel.getSkills().getFirst() instanceof RegularSkill && ownedCopiesInPlay.isEmpty()) {
                    boolean compatibleAtLeastOnce = false;
                    for (FacilityCard outpost : yourOutposts) {
                        if (personnel.isCompatibleWith(outpost)) {
                            compatibleAtLeastOnce = true;
                        }
                    }
                    if (compatibleAtLeastOnce)
                        specialistsNotInPlay.add(personnel);
                }
            }

            Map<PersonnelCard, List<PersonnelCard>> validCombinations = new HashMap<>();

            for (PersonnelCard specialist : specialistsNotInPlay) {
                List<PersonnelCard> validPairings = new LinkedList<>();
                for (PersonnelCard otherSpecialist : specialistsNotInPlay) {
                    boolean validPairing = false;
                    for (FacilityCard outpost : yourOutposts) {
                        if (specialist.isCompatibleWith(outpost) && specialist.isCompatibleWith(otherSpecialist) &&
                                otherSpecialist.isCompatibleWith(outpost) && !specialist.isCopyOf(otherSpecialist)) {
                            validPairing = true;
                        }
                    }
                    if (validPairing)
                        validPairings.add(otherSpecialist);
                }
                validCombinations.put(specialist, validPairings);
            }

            Action downloadAction = new DownloadMultipleCardsToSameCompatibleOutpostAction(
                    Zone.DRAW_DECK, player, thisCard, validCombinations, 2);
            actions.add(downloadAction);
        }
        /* once each mission, your mission specialist may score 5 points when they use their skill to meet a mission
            requirement
         */

        if (actionResult.getType() == ActionResult.Type.START_OF_TURN && player == thisCard.getOwner() &&
                player == game.getCurrentPlayer()) {
            actions.add(new DiscardCardAction(thisCard, player, thisCard));
        }

        return actions;
    }

}