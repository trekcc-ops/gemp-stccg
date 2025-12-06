package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadMultipleCardsToSameCompatibleOutpostAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class Blueprint109_063 extends CardBlueprint {

    // Assign Mission Specialists
    Blueprint109_063() {
        super("109_063");
    }

    public List<TopLevelSelectableAction> getValidResponses(PhysicalCard thisCard, Player player,
                                                            ActionResult actionResult, DefaultGame cardGame)
            throws PlayerNotFoundException {
        ST1EGame stGame = (ST1EGame) cardGame;
        List<TopLevelSelectableAction> actions = new ArrayList<>();
        if (actionResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard &&
                thisCard.isControlledBy(player)) {

            List<FacilityCard> yourOutposts = new LinkedList<>();
            for (PhysicalCard card : Filters.yourFacilitiesInPlay(cardGame, player)) {
                if (card instanceof FacilityCard facilityCard && facilityCard.getFacilityType() == FacilityType.OUTPOST)
                    yourOutposts.add(facilityCard);
            }

            List<PersonnelCard> specialistsNotInPlay = new LinkedList<>();
            for (PhysicalCard card : player.getCardsInDrawDeck()) {
                Collection<PhysicalCard> ownedCopiesInPlay = Filters.filterCardsInPlay(cardGame,
                        Filters.copyOfCard(card), Filters.owner(player.getPlayerId()));
                if (card instanceof PersonnelCard personnel && personnel.getSkills(cardGame).size() == 1 &&
                        personnel.getSkills(cardGame).getFirst() instanceof RegularSkill && ownedCopiesInPlay.isEmpty()) {
                    boolean compatibleAtLeastOnce = false;
                    for (FacilityCard outpost : yourOutposts) {
                        if (personnel.isCompatibleWith((ST1EGame) cardGame, outpost)) {
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
                        if (specialist.isCompatibleWith(stGame, outpost) && specialist.isCompatibleWith(stGame, otherSpecialist) &&
                                otherSpecialist.isCompatibleWith(stGame, outpost) && !specialist.isCopyOf(otherSpecialist)) {
                            validPairing = true;
                        }
                    }
                    if (validPairing)
                        validPairings.add(otherSpecialist);
                }
                validCombinations.put(specialist, validPairings);
            }

            actions.add(new DownloadMultipleCardsToSameCompatibleOutpostAction(cardGame,
                    Zone.DRAW_DECK, player, thisCard, validCombinations, 2));
        }
        /* once each mission, your mission specialist may score 5 points when they use their skill to meet a mission
            requirement
         */

        if (actionResult.getType() == ActionResult.Type.START_OF_TURN &&
                Objects.equals(player.getPlayerId(), thisCard.getOwnerName()) &&
                player == cardGame.getCurrentPlayer()) {
            actions.add(new DiscardSingleCardAction(cardGame, thisCard, player.getPlayerId(), thisCard));
        }

        return actions;
    }

}