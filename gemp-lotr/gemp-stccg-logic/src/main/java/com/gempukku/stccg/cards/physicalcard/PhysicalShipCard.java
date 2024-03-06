package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.movecard.*;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.rules.TextUtils;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PhysicalShipCard extends PhysicalReportableCard1E
        implements AffiliatedCard, AttemptingUnit, CardWithCrew {

    private boolean _docked = false;
    private FacilityCard _dockedAtCard = null;
    private int _rangeAvailable;

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
        actions.removeIf(action -> !action.canBeInitiated());
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
        _game.getGameState().transferCard(this, facilityCard);
        _docked = true;
        _dockedAtCard = facilityCard;
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
        return getAttachedCards();
    }

    public boolean isStaffed() {
            // TODO - Ignores any staffing requirement that is not an Icon1E
            // TODO - Does not require a personnel of matching affiliation aboard
        Map<Icon1E, Long> staffingNeeded = frequencyMap(_blueprint.getStaffing().stream());
        List<List<Icon1E>> staffingIconsAvailable = new LinkedList<>();
        for (PhysicalCard card : getCrew()) {
            if (card instanceof PersonnelCard) {
                List<Icon1E> icons = ((PersonnelCard) card).getIcons();
                if (icons != null) {
                    List<Icon1E> cardIcons = new LinkedList<>(icons);
                    if (cardIcons.contains(Icon1E.COMMAND) && !cardIcons.contains(Icon1E.STAFF))
                        cardIcons.add(Icon1E.STAFF);
                    staffingIconsAvailable.add(cardIcons);
                }
            }
        }
        for (List<Icon1E> combination : Lists.cartesianProduct(staffingIconsAvailable)) {
            boolean staffed = true;
            Map<Icon1E, Long> staffingAvailable = frequencyMap(combination.stream());
            for (Icon1E icon : staffingNeeded.keySet()) {
                if (staffingAvailable.get(icon) == null || staffingAvailable.get(icon) < staffingNeeded.get(icon))
                    staffed = false;
            }
            if (staffed)
                return staffed;
        }
        return false;
    }

    private Map<Icon1E, Long> frequencyMap(Stream<Icon1E> icons) {
        return icons.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    @Override
    public String getTypeSpecificCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Crew",getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty())
                sb.append("<br><b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ").append(TextUtils.getConcatenatedCardLinks(entry.getValue())).append("<br>");
        }

        sb.append("<br><b>").append("Staffing requirements:</b> ");
        if (_blueprint.getStaffing() == null || _blueprint.getStaffing().isEmpty())
            sb.append("<i>none</i>");
        else {
            sb.append("<br>");
            for (Icon1E icon : _blueprint.getStaffing())
                sb.append("<img src='").append(icon.getIconURL()).append("'>");
        }
        if (isStaffed())
            sb.append("<br><i>(Ship is staffed)</i><br>");
        else sb.append("<br><i>(Ship is not staffed)</i><br>");

        sb.append("<br><b>Printed RANGE:</b> ").append( _blueprint.getRange());
        sb.append("<br><b>RANGE available:</b> ").append(_rangeAvailable);

        sb.append("<br><b>Icons:</b> ");

        for (Icon1E icon : Icon1E.values())
            if (hasIcon(icon))
                sb.append(icon.toHTML());

        return sb.toString();
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

    @Override
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
            Affiliation personnelAffiliation = card.getCurrentAffiliation();
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
}