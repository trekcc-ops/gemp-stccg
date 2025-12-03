package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.*;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PhysicalShipCard extends PhysicalReportableCard1E
        implements AffiliatedCard, AttemptingUnit, CardWithCrew, CardWithHullIntegrity {

    private boolean _docked = false;
    @JsonProperty("dockedAtCardId")
    private Integer _dockedAtCardId;
    int _rangeAvailable;
    int _usedRange;
    private int _hullIntegrity = 100;

    public PhysicalShipCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
                // TODO - Implement land, take off, cloak
            if (isControlledBy(player.getPlayerId())) {
                if (hasTransporters())
                    actions.add(new BeamCardsAction(cardGame, player, this));
                if (isDocked())
                    actions.add(new WalkCardsAction(cardGame, player, this));
                if (isStaffed()) {
                    if (isDocked())
                        actions.add(new UndockAction(cardGame, player.getPlayerId(), this));
                    if (!isDocked())
                        actions.add(new DockAction(player, this, _game));
                    try {
                        actions.add(new FlyShipAction(player, this, _game));
                    } catch(InvalidGameLogicException exp) {
                        _game.sendErrorMessage(exp);
                    }
                }
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(_game));
        return actions;
    }

    public boolean isDocked() { return _docked; }

    public void dockAtFacility(FacilityCard facilityCard) {
        _docked = true;
        _dockedAtCardId = facilityCard.getCardId();
    }

    public void undockFromFacility() throws InvalidGameOperationException {
        _docked = false;
        _dockedAtCardId = null;
        setZone(Zone.AT_LOCATION);
        detach();
    }

    public PhysicalCard getDockedAtCard(DefaultGame cardGame) {
        try {
            return cardGame.getCardFromCardId(_dockedAtCardId);
        } catch(Exception exp) {
            return null;
        }
    }

    public boolean isDockedAtCardId(int facilityCardId) {
        return _dockedAtCardId != null && _dockedAtCardId == facilityCardId;
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
                if (icons != null && !icons.isEmpty()) {
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
                if (staffingAvailable.get(icon) == null) {
                    staffed = false;
                } else if (staffingAvailable.get(icon) < staffingNeeded.get(icon)) {
                    staffed = false;
                }
            }
            if (staffed) {
                return true;
            }
        }
        return false;
    }

    private Map<CardIcon, Long> frequencyMap(Stream<CardIcon> icons) {
        return icons.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public int getFullRange() {
        return (int) _game.getGameState().getModifiersQuerying().getAttribute(this, CardAttribute.RANGE);
    }

    public float getWeapons(DefaultGame cardGame) {
        return _game.getGameState().getModifiersQuerying().getAttribute(this, CardAttribute.WEAPONS);
    }

    public float getShields(DefaultGame cardGame) {
        return _game.getGameState().getModifiersQuerying().getAttribute(this, CardAttribute.SHIELDS);
    }

    public int getRangeAvailable() {
        return Math.max(0, getFullRange() - _usedRange);
    }

    public void useRange(int range) {
        _usedRange = _usedRange + range;
    }

    public void restoreRange() {
        _usedRange = 0;
    }

    public boolean canAttemptMission(MissionLocation mission) {
            if (_currentGameLocation != mission)
                return false;
            if (_docked)
                return false;
            // TODO - Does not include logic for dual missions
            if (mission.getMissionType() != MissionType.SPACE)
                return false;
            // TODO - Does not include a check for infiltrators

            // Ship with no unstopped crew can't attempt a mission
            if (getAttemptingPersonnel().isEmpty())
                return false;

            // Check for affiliation requirements
            if (_blueprint.canAnyAttempt())
                return true;
            if (_blueprint.canAnyExceptBorgAttempt() && _currentAffiliation != Affiliation.BORG)
                return true;
            boolean matchesShip = false;
            boolean matchesMission = false;
            for (PersonnelCard card : getAttemptingPersonnel()) {
                Affiliation personnelAffiliation = card.getCurrentAffiliation();
                if (personnelAffiliation == _currentAffiliation)
                    matchesShip = true;
                if (mission.getAffiliationIcons(_ownerName).contains(personnelAffiliation))
                    matchesMission = true;
            }
            return matchesShip && matchesMission;
    }


    public Collection<PersonnelCard> getAllPersonnel() {
        return getPersonnelInCrew();
    }

    public List<CardIcon> getStaffingRequirements() {
        return _blueprint.getStaffing();
    }

    public void applyDamage(Integer damageAmount) {
        _hullIntegrity = _hullIntegrity - damageAmount;
    }

    public int getHullIntegrity() {
        return _hullIntegrity;
    }

    public boolean isExposed() {
        // TODO - can't be cloaked, landed, or carried
        return !_docked;
    }

    public Collection<PersonnelCard> getPersonnelAboard() {
        // TODO - Doesn't include intruders
        return getPersonnelInCrew();
    }
}