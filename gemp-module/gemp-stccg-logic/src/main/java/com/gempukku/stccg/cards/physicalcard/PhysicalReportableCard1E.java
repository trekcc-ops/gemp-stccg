package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class PhysicalReportableCard1E extends PhysicalNounCard1E {
    private AwayTeam _awayTeam;
    public PhysicalReportableCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }
    public boolean canReportToFacility(FacilityCard facility) {
        if (_blueprint.getCardType() == CardType.EQUIPMENT && facility.isUsableBy(_owner.getPlayerId()))
            return true;
        for (Affiliation affiliation : getAffiliationOptions())
            if (canReportToFacilityAsAffiliation(facility, affiliation))
                return true;
        return false;
    }
    public boolean canReportToFacilityAsAffiliation(FacilityCard facility, Affiliation affiliation) {
            /* Normally, Personnel, Ship, and Equipment cards play at a usable, compatible outpost or headquarters
                in their native quadrant. */
        // TODO - Does not perform any compatibility checks other than affiliation
        if ((facility.getFacilityType() == FacilityType.OUTPOST || facility.getFacilityType() == FacilityType.HEADQUARTERS) &&
                facility.isUsableBy(_owner.getPlayerId()) && facility.getCurrentQuadrant() == getNativeQuadrant())
            return isCompatibleWithCardAndItsCrewAsAffiliation(facility, affiliation);
        else return false;
    }

    public boolean isCompatibleWithCardAndItsCrewAsAffiliation(CardWithCrew cardWithCrew,
                                                               Affiliation affiliation) {
        if (!cardWithCrew.isCompatibleWith(affiliation))
            return false;
        for (PhysicalCard card : cardWithCrew.getCrew()) {
            if (card instanceof PersonnelCard personnel)
                if (!personnel.isCompatibleWith(affiliation))
                    return false;
        }
        return true;
    }

    public void reportToFacility(FacilityCard facility) throws InvalidGameLogicException {
        setLocation(facility.getLocation());
        _game.getGameState().attachCard(this, facility);
    }

    @Override
    public Action getPlayCardAction() { return createReportCardAction(); }

    @Override
    public Action getPlayCardAction(boolean forFree) { return createReportCardAction(forFree); }

    public Action createReportCardAction() {
        return new ReportCardAction(this, false);
    }
    public Action createReportCardAction(boolean forFree) { return new ReportCardAction(this, forFree); }

    public void leaveAwayTeam() {
        _awayTeam.remove(this);
        _awayTeam = null;
    }

    public void addToAwayTeam(AwayTeam awayTeam) {
        _awayTeam = awayTeam;
        _awayTeam.add(this);
    }

    public void joinEligibleAwayTeam(MissionLocation mission) {
        // TODO - Assumes owner is the owner of away teams. Won't work for some scenarios - temporary control, captives, infiltrators, etc.
        // TODO - When there are multiple eligible away teams, there should be a player decision
        for (AwayTeam awayTeam : mission.getYourAwayTeamsOnSurface(_owner).toList()) {
            if (awayTeam.isCompatibleWith(this) && _awayTeam == null) {
                addToAwayTeam(awayTeam);
            }
        }
        if (_awayTeam == null) {
            AwayTeam awayTeam = _game.getGameState().createNewAwayTeam(_owner, mission);
            addToAwayTeam(awayTeam);
        }
    }


    public AwayTeam getAwayTeam() { return _awayTeam; }
}