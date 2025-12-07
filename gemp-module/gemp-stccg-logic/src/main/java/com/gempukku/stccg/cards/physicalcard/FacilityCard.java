package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.WalkCardsAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(value = { "cardType", "hasUniversalIcon", "imageUrl", "isInPlay", "title", "uniqueness" },
        allowGetters = true)
public class FacilityCard extends AffiliatedCard implements CardWithCrew, CardWithHullIntegrity {

    private final FacilityType _facilityType;
    private int _hullIntegrity = 100;

    @JsonCreator
    public FacilityCard(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("owner")
            String ownerName,
            @JsonProperty("blueprintId")
            String blueprintId,
            @JacksonInject
            CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {
        super(cardId, ownerName, blueprintLibrary.getCardBlueprint(blueprintId));
        _facilityType = _blueprint.getFacilityType();
    }


    public FacilityCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
        _facilityType = blueprint.getFacilityType();
    }

    public FacilityType getFacilityType() {
        return _facilityType;
    }


    @Override
    public boolean isControlledBy(String playerId) {
        // TODO - Need to set modifiers for when cards get temporary control
        // TODO - shared control of headquarters not implemented
        if (!isInPlay()) {
            return false;
        } else {
            return playerId.equals(_ownerName);
        }
    }

    public boolean isUsableBy(String playerId) {
        return isControlledBy(playerId);
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (cardGame.getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (hasTransporters() && isControlledBy(player.getPlayerId())) {
                actions.add(new BeamCardsAction(cardGame, player, this));
            }
            if (!Filters.filter(getAttachedCards(cardGame), cardGame, Filters.your(player), Filters.personnel).isEmpty()) {
                actions.add(new WalkCardsAction(cardGame, player, this));
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(cardGame));
        return actions;
    }

    public List<TopLevelSelectableAction> createSeedCardActions(DefaultGame cardGame) {
        return List.of(new SeedOutpostAction(cardGame, this));
        // TODO - Add actions for non-outposts
    }


    public Collection<PhysicalCard> getDockedShips(DefaultGame cardGame) {
        return Filters.filter(getAttachedCards(cardGame), cardGame, Filters.ship);
    }

    public Collection<PhysicalCard> getCrew(DefaultGame cardGame) {
        return Filters.filter(getAttachedCards(cardGame), cardGame, Filters.or(Filters.personnel, Filters.equipment));
    }

    public boolean isOutpost() {
        return getFacilityType() == FacilityType.OUTPOST;
    }

    public void applyDamage(Integer damageAmount) {
        _hullIntegrity = _hullIntegrity - damageAmount;
    }

    public int getHullIntegrity() {
        return _hullIntegrity;
    }

    public float getWeapons(DefaultGame cardGame) {
        return cardGame.getAttribute(this, CardAttribute.WEAPONS);
    }

    public float getShields(DefaultGame cardGame) {
        return cardGame.getAttribute(this, CardAttribute.SHIELDS);
    }

    public Collection<PersonnelCard> getPersonnelAboard(DefaultGame cardGame) {
        return getPersonnelInCrew(cardGame);
    }

}