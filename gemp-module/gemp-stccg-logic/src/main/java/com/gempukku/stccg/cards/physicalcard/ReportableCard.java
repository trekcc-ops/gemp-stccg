package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public interface ReportableCard extends CardWithCompatibility {

    void leaveAwayTeam(ST1EGame cardGame);

    boolean isInAnAwayTeam();

    void addToAwayTeam(AwayTeam awayTeam);

    AwayTeam getAwayTeam();

    default boolean canReportToFacility(FacilityCard facility, ST1EGame stGame) {
        if (getCardType() == CardType.EQUIPMENT && facility.isUsableBy(getOwnerName()))
            return true;
        if (this instanceof AffiliatedCard affiliatedCard) {
            for (Affiliation affiliation : affiliatedCard.getAffiliationOptions())
                if (affiliatedCard.canReportToFacilityAsAffiliation(facility, affiliation, stGame))
                    return true;
        }
        return false;
    }

    default void joinEligibleAwayTeam(ST1EGame game, MissionLocation mission) {
        // TODO - Assumes owner is the owner of away teams. Won't work for some scenarios - temporary control, captives, infiltrators, etc.
        // TODO - When there are multiple eligible away teams, there should be a player decision
        for (AwayTeam awayTeam : mission.getYourAwayTeamsOnSurface(game, getOwnerName()).toList()) {
            if (awayTeam.isCompatibleWith(game, this) && !isInAnAwayTeam()) {
                addToAwayTeam(awayTeam);
            }
        }
        if (!isInAnAwayTeam()) {
            AwayTeam awayTeam = game.getGameState().createNewAwayTeam(getOwnerName(), mission);
            addToAwayTeam(awayTeam);
        }
    }

    @JsonProperty("isStopped")
    boolean isStopped();

}