package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;

public class PhysicalReportableCard1E extends PhysicalNounCard1E {
    public PhysicalReportableCard1E(ST1EGame game, int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(game, cardId, blueprintId, owner, blueprint);
    }
    public boolean canReportToFacility(PhysicalFacilityCard facility) {
        for (Affiliation affiliation : _affiliationOptions)
            if (canReportToFacilityAsAffiliation(facility, affiliation))
                return true;
        return false;
    }
    public boolean canReportToFacilityAsAffiliation(PhysicalFacilityCard facility, Affiliation affiliation) {
            /* Normally, Personnel, Ship, and Equipment cards play at a usable, compatible outpost or headquarters
                in their native quadrant. */
        if (facility.getFacilityType() == FacilityType.OUTPOST || facility.getFacilityType() == FacilityType.HEADQUARTERS)
          return facility.isCompatibleWith(affiliation) &&
                  facility.isUsableBy(_owner) &&
                  facility.getCurrentQuadrant() == _nativeQuadrant;
        else
            return false;
    }

    @Override
    public boolean canBePlayed() {
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            for (PhysicalFacilityCard facility : location.getOutposts())
                if (this.canReportToFacility(facility))
                    return true;
        }
        return false;
    }

}