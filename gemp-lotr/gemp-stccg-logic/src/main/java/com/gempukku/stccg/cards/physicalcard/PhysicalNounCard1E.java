package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.google.common.collect.Iterables;

import java.util.Set;

public class PhysicalNounCard1E extends ST1EPhysicalCard {
    protected final Set<Affiliation> _affiliationOptions;
    protected Affiliation _currentAffiliation; // TODO - NounCard class may include Equipment or other cards with no affiliation
    protected final Quadrant _nativeQuadrant;
    public PhysicalNounCard1E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _affiliationOptions = blueprint.getAffiliations();
        if (_affiliationOptions.size() == 1)
            _currentAffiliation = Iterables.getOnlyElement(_affiliationOptions);
        _nativeQuadrant = blueprint.getQuadrant();
    }

    public boolean isMultiAffiliation() { return _affiliationOptions.size() > 1; }
    public Affiliation getCurrentAffiliation() { return _currentAffiliation; }
    public void setCurrentAffiliation(Affiliation affiliation) {
        _currentAffiliation = affiliation;
        if (_affiliationOptions.size() > 1) {
            if (_attachedTo instanceof MissionCard mission &&
                    this instanceof PhysicalReportableCard1E reportable) {
                if (reportable.getAwayTeam().canBeDisbanded()) {
                    reportable.getAwayTeam().disband();
                } else {
                    if (reportable.getAwayTeam() != null && !reportable.getAwayTeam().isCompatibleWith(reportable))
                        reportable.leaveAwayTeam();
                    if (reportable.getAwayTeam() == null)
                        reportable.joinEligibleAwayTeam(mission);
                }
            }
            String newImageUrl = _blueprint.getAffiliationImageUrl(affiliation);
            if (newImageUrl != null) {
                _imageUrl = newImageUrl;
                _game.getGameState().sendUpdatedCardImageToClient(this);
            }
        }
    }

    public Set<Affiliation> getAffiliationOptions() { return _affiliationOptions; }
    public boolean isCompatibleWith(Affiliation affiliation) {
        if (getCurrentAffiliation() == affiliation)
            return true;
        if (getCurrentAffiliation() == Affiliation.BORG || affiliation == Affiliation.BORG)
            return false;
        return getCurrentAffiliation() == Affiliation.NON_ALIGNED || affiliation == Affiliation.NON_ALIGNED;
    }

    public boolean isCompatibleWith(PhysicalNounCard1E card) { return isCompatibleWith(card.getCurrentAffiliation()); }
    public Quadrant getCurrentQuadrant() {
        return _currentLocation.getQuadrant();
    }

    @Override
    public boolean hasTransporters() {
        if (_blueprint.getCardType() == CardType.SHIP || _blueprint.getCardType() == CardType.FACILITY) {
            return !_blueprint.hasNoTransporters();
        } else return false;
    }

    public boolean isAffiliation(Affiliation affiliation) {
        if (_zone.isInPlay())
            return _currentAffiliation == affiliation;
        else return _affiliationOptions.contains(affiliation);
    }

}