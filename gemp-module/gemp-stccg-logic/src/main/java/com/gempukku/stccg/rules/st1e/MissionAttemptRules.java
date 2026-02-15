package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;

public class MissionAttemptRules {

    public static boolean canShipAttemptMission(ShipCard ship, MissionLocation missionLocation, DefaultGame cardGame,
                                         String performingPlayerName) throws InvalidGameLogicException {
        if (ship.getLocationId() != missionLocation.getLocationId()) {
            return false;
        }
        if (ship.isDocked())
            return false;
        // TODO - Does not include logic for dual missions
        if (missionLocation.getMissionType() != MissionType.SPACE)
            return false;
        // TODO - Does not include a check for infiltrators

        // Ship with no unstopped crew can't attempt a mission
        if (ship.getAttemptingPersonnel(cardGame).isEmpty())
            return false;

        MissionCard missionCard = missionLocation.getMissionForPlayer(performingPlayerName);


        // Check for affiliation requirements
        if (missionCard.getBlueprint().canAnyAttempt())
            return true;
        if (missionCard.getBlueprint().canAnyExceptBorgAttempt() && !ship.isAffiliation(Affiliation.BORG))
            return true;
        boolean matchesShip = false;
        boolean matchesMission = false;
        for (PersonnelCard card : ship.getAttemptingPersonnel(cardGame)) {
            if (card.matchesAffiliationOf(ship)) {
                matchesShip = true;
            }
            for (Affiliation affiliation : missionLocation.getAffiliationIcons(cardGame, performingPlayerName)) {
                if (card.isAffiliation(affiliation)) {
                    matchesMission = true;
                }
            }
        }
        return matchesShip && matchesMission;

    }


}