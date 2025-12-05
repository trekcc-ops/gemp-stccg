package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Set;

public class PhysicalNounCard1E extends ST1EPhysicalCard {
    protected Affiliation _currentAffiliation; // TODO - NounCard class may include Equipment or other cards with no affiliation
    public PhysicalNounCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        if (blueprint.getAffiliations().size() == 1)
            _currentAffiliation = Iterables.getOnlyElement(blueprint.getAffiliations());
    }

    protected Quadrant getNativeQuadrant() { return _blueprint.getQuadrant(); }
    public boolean isMultiAffiliation() { return getAffiliationOptions().size() > 1; }

    @JsonProperty("affiliation")
    public Affiliation getCurrentAffiliation() { return _currentAffiliation; }

    public void setCurrentAffiliation(Affiliation affiliation) {
        _currentAffiliation = affiliation;
    }
    public void changeAffiliation(Affiliation affiliation) throws PlayerNotFoundException {
        setCurrentAffiliation(affiliation);
        if (getAffiliationOptions().size() > 1) {
            if (this instanceof PhysicalReportableCard1E reportable &&
                    _currentGameLocation instanceof MissionLocation missionLocation) {
                if (reportable.getAwayTeam().canBeDisbanded(_game)) {
                    reportable.getAwayTeam().disband(_game);
                } else {
                    if (reportable.getAwayTeam() != null && !reportable.getAwayTeam().isCompatibleWith(reportable))
                        reportable.leaveAwayTeam(_game);
                    if (reportable.getAwayTeam() == null)
                        reportable.joinEligibleAwayTeam(missionLocation);
                }
            }
        }
    }

    public Set<Affiliation> getAffiliationOptions() { return _blueprint.getAffiliations(); }
    public boolean isCompatibleWith(Affiliation affiliation) {
            // TODO - Compatibility should check against a specific card, not an affiliation
        if (_currentAffiliation == affiliation)
            return true;
        if (_currentAffiliation == Affiliation.BORG || affiliation == Affiliation.BORG)
            return false;
        return _currentAffiliation == Affiliation.NON_ALIGNED || affiliation == Affiliation.NON_ALIGNED;
    }

    public boolean isCompatibleWith(PhysicalNounCard1E card) {
        if (_blueprint.doesNotWorkWithPerRestrictionBox(this, card))
            return false;
        else
            return _game.getRules().areCardsCompatiblePerRules(this, card);
    }

    public boolean isInQuadrant(Quadrant quadrant) {
        return _currentGameLocation.isInQuadrant(quadrant);
    }

    @Override
    public boolean hasTransporters() {
        return _blueprint.getCardType() == CardType.SHIP || _blueprint.getCardType() == CardType.FACILITY; // TODO - Cards with no transporters
    }

    public boolean isAffiliation(Affiliation affiliation) {
        if (isInPlay())
            return _currentAffiliation == affiliation;
        else return getAffiliationOptions().contains(affiliation);
    }

}