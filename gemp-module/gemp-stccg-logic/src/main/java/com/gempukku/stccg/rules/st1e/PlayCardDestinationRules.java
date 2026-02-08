package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.rules.UndefinedRuleException;

import java.util.Collection;

public class PlayCardDestinationRules {

    public static boolean isLocationValidPlayCardDestinationForFacilityPerRules(
            ST1EGame cardGame, GameLocation location, FacilityCard facilityEnteringPlay,
            Class<? extends PlayCardAction> actionClass, Player performingPlayer,
            Collection<Affiliation> affiliationOptions
    ) throws UndefinedRuleException {

        final boolean isSeeding = SeedCardAction.class.isAssignableFrom(actionClass);

        // Each facility must be seeded in its native quadrant
        if (isSeeding && !location.isInQuadrant(facilityEnteringPlay.getNativeQuadrant())) {
            return false;
        }

        /* You may not seed or build a facility at a location where you already own one (unless permitted by a
            card that allows them to co-exist).
         */
        Collection<PhysicalCard> facilitiesOwnedByPlayerHere = Filters.filteredCardsYouOwnInPlay(
                cardGame, performingPlayer, CardType.FACILITY, Filters.atLocation(location));
        if (!facilitiesOwnedByPlayerHere.isEmpty()) {
            return false;
            // TODO - Need to add a check for cards that can coexist
        }

        if (!facilityEnteringPlay.isOutpost()) {
            // TODO - Most Facilities state on their cards where they may be seeded
            throw new UndefinedRuleException("No rule defined yet for non-outpost facilities entering play");
        } else if (isSeeding) {
            // Outposts may never be seeded at the homeworld of any affiliation
            if (location.isHomeworld()) {
                return false;
            }

            // Otherwise, outposts may be seeded at any mission in their native quadrant with a matching affiliation icon
            // (No need to check for native quadrant again; that was performed above)
            // TODO - This may not be a sufficient check for multi-affiliation cards under special reporting
            return location instanceof MissionLocation mission &&
                    mission.hasMatchingAffiliationIcon(performingPlayer, affiliationOptions);
        } else {
            return location instanceof MissionLocation missionLocation && !missionLocation.isHomeworld();
        }
    }
}