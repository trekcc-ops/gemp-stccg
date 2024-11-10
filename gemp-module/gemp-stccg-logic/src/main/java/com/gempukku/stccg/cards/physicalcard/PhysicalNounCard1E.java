package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.SnapshotData;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;
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
    public Affiliation getAffiliation() { return _currentAffiliation; }

    public void setCurrentAffiliation(Affiliation affiliation) {
        _currentAffiliation = affiliation;
    }
    public void changeAffiliation(Affiliation affiliation) {
        setCurrentAffiliation(affiliation);
        if (getAffiliationOptions().size() > 1) {
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
            _game.getGameState().sendUpdatedCardImageToClient(this);
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
    public Quadrant getCurrentQuadrant() {
        return _currentLocation.getQuadrant();
    }

    @Override
    public boolean hasTransporters() {
        return _blueprint.getCardType() == CardType.SHIP || _blueprint.getCardType() == CardType.FACILITY; // TODO - Cards with no transporters
    }

    public boolean isAffiliation(Affiliation affiliation) {
        if (_zone.isInPlay())
            return _currentAffiliation == affiliation;
        else return getAffiliationOptions().contains(affiliation);
    }

    @Override
    public ST1EPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes
        // TODO SNAPSHOT - Doesn't have awayTeam specified

        PhysicalNounCard1E newCard = new PhysicalNounCard1E(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);

        for (PhysicalCard card : _cardsSeededUnderneath)
            newCard.addCardToSeededUnder(snapshotData.getDataForSnapshot(card));

        for (Map.Entry<Player, List<PhysicalCard>> entry : _cardsPreSeededUnderneath.entrySet())
            for (PhysicalCard card : entry.getValue())
                newCard.addCardToPreSeeds(snapshotData.getDataForSnapshot(card), entry.getKey());

        newCard._currentAffiliation = _currentAffiliation;

        return newCard;
    }

}