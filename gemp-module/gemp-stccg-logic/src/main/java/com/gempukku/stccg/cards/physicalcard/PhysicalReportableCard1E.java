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
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collections;

public class PhysicalReportableCard1E extends PhysicalNounCard1E {
    private AwayTeam _awayTeam;
    public PhysicalReportableCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }
    public boolean canReportToFacility(FacilityCard facility) {
        if (_blueprint.getCardType() == CardType.EQUIPMENT && facility.isUsableBy(_ownerName))
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
                facility.isUsableBy(_ownerName) && facility.isInQuadrant(this.getNativeQuadrant()))
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
        setLocation(_game, facility.getGameLocation());

        attachTo(facility);
        gameState.addCardToInPlay(_game, this);
        _zone = Zone.ATTACHED;

        if (this instanceof PhysicalShipCard ship) {
            ship.dockAtFacility(facility);
        }
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame) {
        return createReportCardAction(cardGame, false);
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame, boolean forFree) {
        return createReportCardAction(cardGame, forFree);
    }

    public TopLevelSelectableAction createReportCardAction() {
        return createReportCardAction(false);
    }
    public TopLevelSelectableAction createReportCardAction(boolean forFree) {
        return new ReportCardAction(_game, this, forFree);
    }

    public TopLevelSelectableAction createReportCardAction(DefaultGame cardGame, boolean forFree) {
        return new ReportCardAction(cardGame, this, forFree);
    }

    public void leaveAwayTeam(ST1EGame cardGame) {
        _awayTeam.remove(cardGame, this);
        _awayTeam = null;
    }


    public void addToAwayTeam(AwayTeam awayTeam) {
        _awayTeam = awayTeam;
        _awayTeam.add(this);
    }

    public void joinEligibleAwayTeam(MissionLocation mission) throws PlayerNotFoundException {
        // TODO - Assumes owner is the owner of away teams. Won't work for some scenarios - temporary control, captives, infiltrators, etc.
        // TODO - When there are multiple eligible away teams, there should be a player decision
        for (AwayTeam awayTeam : mission.getYourAwayTeamsOnSurface(_game, _ownerName).toList()) {
            if (awayTeam.isCompatibleWith(this) && _awayTeam == null) {
                addToAwayTeam(awayTeam);
            }
        }
        if (_awayTeam == null) {
            AwayTeam awayTeam = _game.getGameState().createNewAwayTeam(_game.getPlayer(_ownerName), mission);
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