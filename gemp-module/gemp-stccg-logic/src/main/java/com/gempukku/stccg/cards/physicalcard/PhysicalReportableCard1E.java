package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.security.InvalidParameterException;
import java.util.Collections;

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
                facility.isUsableBy(_owner.getPlayerId()) && facility.isInQuadrant(this.getNativeQuadrant()))
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
        ST1EGameState gameState = _game.getGameState();

        gameState.removeCardsFromZoneWithoutSendingToClient(_game, Collections.singleton(this));
        setLocation(facility.getGameLocation());

        attachTo(facility);
        gameState.addCardToInPlay(this);
        _zone = Zone.ATTACHED;

        if (this instanceof PhysicalShipCard ship) {
            ship.dockAtFacility(facility);
        }
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction() { return createReportCardAction(); }

    @Override
    public TopLevelSelectableAction getPlayCardAction(boolean forFree) { return createReportCardAction(forFree); }

    public TopLevelSelectableAction createReportCardAction() {
        return createReportCardAction(false);
    }
    public TopLevelSelectableAction createReportCardAction(boolean forFree) {
        return new ReportCardAction(this, forFree);
    }

    public void leaveAwayTeam(ST1EGame cardGame) {
        _awayTeam.remove(cardGame, this);
        _awayTeam = null;
    }


    public void addToAwayTeam(AwayTeam awayTeam) {
        _awayTeam = awayTeam;
        _awayTeam.add(this);
    }

    public void joinEligibleAwayTeam(MissionLocation mission) {
        // TODO - Assumes owner is the owner of away teams. Won't work for some scenarios - temporary control, captives, infiltrators, etc.
        // TODO - When there are multiple eligible away teams, there should be a player decision
        for (AwayTeam awayTeam : mission.getYourAwayTeamsOnSurface(_game, _owner).toList()) {
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

    @Override
    @JsonProperty("isStopped")
    public boolean isStopped() {
        return _isStopped;
    }

}