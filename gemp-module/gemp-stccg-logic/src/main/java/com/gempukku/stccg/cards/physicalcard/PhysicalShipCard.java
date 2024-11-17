package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.movecard.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.SnapshotData;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PhysicalShipCard extends PhysicalReportableCard1E
        implements AffiliatedCard, AttemptingUnit, CardWithCrew {

    private boolean _docked = false;
    private FacilityCard _dockedAtCard = null;
    int _rangeAvailable;

    public PhysicalShipCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _rangeAvailable = _blueprint.getRange();
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
                // TODO - Implement land, take off, cloak
            if (isControlledBy(player.getPlayerId())) {
                if (hasTransporters())
                    actions.add(new BeamCardsAction(player, this));
                if (isDocked())
                    actions.add(new WalkCardsAction(player, this));
                if (isStaffed()) {
                    if (isDocked())
                        actions.add(new UndockAction(player, this));
                    if (!isDocked())
                        actions.add(new DockAction(player, this));
                    actions.add(new FlyShipAction(player, this));
                }
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(_game));
        return actions;
    }

    public boolean isDocked() { return _docked; }

    @Override
    public void reportToFacility(FacilityCard facility) {
        setLocation(facility.getLocation()); // TODO - What happens if the facility doesn't allow docking?
        _game.getGameState().attachCard(this, facility);
        _docked = true;
        _dockedAtCard = facility;
    }

    public void dockAtFacility(FacilityCard facilityCard) {
        _docked = true;
        _dockedAtCard = facilityCard;
    }

    public Player getPlayer() {
        // TODO - Should be controller
        return _owner;
    }

    public void undockFromFacility() {
        _docked = false;
        _dockedAtCard = null;
        _game.getGameState().detachCard(this, Zone.AT_LOCATION);
    }

    public PhysicalCard getDockedAtCard() {
        return _dockedAtCard;
    }

    public Collection<PhysicalCard> getCrew() {
        return getAttachedCards(_game);
    }

    public boolean isStaffed() {
            // TODO - Ignores any staffing requirement that is not a CardIcon
            // TODO - Does not require a personnel of matching affiliation aboard
        Map<CardIcon, Long> staffingNeeded = frequencyMap(_blueprint.getStaffing().stream());
        List<List<CardIcon>> staffingIconsAvailable = new LinkedList<>();
        for (PhysicalCard card : getCrew()) {
            if (card instanceof PersonnelCard personnelCard) {
                List<CardIcon> icons = personnelCard.getIcons();
                if (icons != null) {
                    List<CardIcon> cardIcons = new LinkedList<>(icons);
                    if (cardIcons.contains(CardIcon.COMMAND) && !cardIcons.contains(CardIcon.STAFF))
                        cardIcons.add(CardIcon.STAFF);
                    staffingIconsAvailable.add(cardIcons);
                }
            }
        }
        for (List<CardIcon> combination : Lists.cartesianProduct(staffingIconsAvailable)) {
            boolean staffed = true;
            Map<CardIcon, Long> staffingAvailable = frequencyMap(combination.stream());
            for (CardIcon icon : staffingNeeded.keySet()) {
                if (staffingAvailable.get(icon) == null || staffingAvailable.get(icon) < staffingNeeded.get(icon))
                    staffed = false;
            }
            if (staffed)
                return true;
        }
        return false;
    }

    private Map<CardIcon, Long> frequencyMap(Stream<CardIcon> icons) {
        return icons.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public int getRangeAvailable() {
        return _rangeAvailable;
    }

    public void useRange(int range) {
        _rangeAvailable = _rangeAvailable - range;
    }

    public void restoreRange() {
        _rangeAvailable = _blueprint.getRange();
    }

    public boolean canAttemptMission(MissionCard mission) {
        if (_currentLocation != mission.getLocation())
            return false;
        if (_docked)
            return false;
                // TODO - Does not include logic for dual missions
        if (mission.getBlueprint().getMissionType() != MissionType.SPACE)
            return false;
            // TODO - Does not include a check for infiltrators
        boolean matchesShip = false;
        boolean matchesMission = false;
        for (PersonnelCard card : getAttemptingPersonnel()) {
            Affiliation personnelAffiliation = card.getAffiliation();
            if (personnelAffiliation == _currentAffiliation)
                matchesShip = true;
            if (mission.getAffiliationIcons(_owner.getPlayerId()).contains(personnelAffiliation))
                matchesMission = true;
        }
        return matchesShip && matchesMission;
    }

    public Collection<PersonnelCard> getAllPersonnel() {
        return getPersonnelInCrew();
    }

    @Override
    public ST1EPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        PhysicalShipCard newCard = new PhysicalShipCard(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
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
        newCard._docked = _docked;
        newCard._dockedAtCard = snapshotData.getDataForSnapshot(_dockedAtCard);
        newCard._rangeAvailable = _rangeAvailable;

        return newCard;
    }
    
}