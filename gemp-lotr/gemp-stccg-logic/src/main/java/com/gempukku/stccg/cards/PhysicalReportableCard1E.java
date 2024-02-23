package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ReportCardAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class PhysicalReportableCard1E extends PhysicalNounCard1E {
    public PhysicalReportableCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
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
                  facility.isUsableBy(_ownerName) &&
                  facility.getCurrentQuadrant() == _nativeQuadrant;
        else
            return false;
    }

    public void reportToFacility(PhysicalFacilityCard facility) {
        setCurrentLocation(facility.getLocation());
        _game.getGameState().attachCard(this, facility);
    }

    public Action createReportCardAction() {
        return new ReportCardAction(this);
    }
}