package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.*;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@JsonIgnoreProperties(value = { "cardType", "hasUniversalIcon", "imageUrl", "isInPlay", "title", "uniqueness" },
        allowGetters = true)
public class ShipCard extends AffiliatedCard implements AttemptingUnit, CardWithCrew, CardWithHullIntegrity,
        ReportableCard {

    private boolean _docked = false;
    @JsonProperty("dockedAtCardId")
    private Integer _dockedAtCardId;
    int _rangeAvailable;
    int _usedRange;
    private int _hullIntegrity = 100;

    @JsonCreator
    public ShipCard(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("owner")
            String ownerName,
            @JsonProperty("blueprintId")
            String blueprintId,
            @JacksonInject
            CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {
        super(cardId, ownerName, blueprintLibrary.getCardBlueprint(blueprintId));
    }


    public ShipCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (cardGame.getCurrentPhase() == Phase.EXECUTE_ORDERS && cardGame instanceof ST1EGame stGame) {
                // TODO - Implement land, take off, cloak
            if (isControlledBy(player.getPlayerId())) {
                if (hasTransporters())
                    actions.add(new BeamCardsAction(cardGame, player, this));
                if (isDocked())
                    actions.add(new WalkCardsAction(cardGame, player, this));
                if (isStaffed(stGame)) {
                    if (isDocked())
                        actions.add(new UndockAction(cardGame, player.getPlayerId(), this));
                    if (!isDocked())
                        actions.add(new DockAction(player, this, stGame));
                    try {
                        actions.add(new FlyShipAction(player, this, stGame));
                    } catch(InvalidGameLogicException exp) {
                        cardGame.sendErrorMessage(exp);
                    }
                }
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(cardGame));
        return actions;
    }

    public boolean isDocked() { return _docked; }

    public void dockAtFacility(FacilityCard facilityCard) {
        _docked = true;
        _dockedAtCardId = facilityCard.getCardId();
    }

    public void undockFromFacility() {
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

    public Collection<PhysicalCard> getCrew(DefaultGame cardGame) {
        return Filters.filter(getAttachedCards(cardGame), cardGame, Filters.your(getControllerName()));
    }

    public boolean isStaffed(DefaultGame cardGame) {
        // TODO - Ignores any staffing requirement that is not a CardIcon
        // TODO - Does not require a personnel of matching affiliation aboard
        Map<CardIcon, Long> staffingNeeded = frequencyMap(_blueprint.getStaffing().stream());
        List<List<CardIcon>> staffingIconsAvailable = new LinkedList<>();
        for (PhysicalCard card : getCrew(cardGame)) {
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

    public int getFullRange(DefaultGame cardGame) {
        return (int) cardGame.getAttribute(this, CardAttribute.RANGE);
    }


    public float getWeapons(DefaultGame cardGame) {
        return cardGame.getAttribute(this, CardAttribute.WEAPONS);
    }

    public float getShields(DefaultGame cardGame) {
        return cardGame.getAttribute(this, CardAttribute.SHIELDS);
    }

    public int getRangeAvailable(DefaultGame cardGame) {
        return Math.max(0, getFullRange(cardGame) - _usedRange);
    }


    public void useRange(int range) {
        _usedRange = _usedRange + range;
    }

    public void restoreRange() {
        _usedRange = 0;
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

    public Collection<PersonnelCard> getPersonnelAboard(DefaultGame cardGame) {
        // TODO - Doesn't include intruders
        return getPersonnelInCrew(cardGame);
    }


    @Override
    public Collection<PersonnelCard> getAllPersonnel(DefaultGame cardGame) {
        return getPersonnelInCrew(cardGame);
    }
}