package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.WalkCardsAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FacilityCard extends PhysicalNounCard1E implements AffiliatedCard, CardWithCrew, CardWithHullIntegrity {

    private int _hullIntegrity = 100;
    public FacilityCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }
    public FacilityType getFacilityType() {
        return getBlueprint().getFacilityType();
    }

    public boolean canSeedAtMission(MissionLocation mission) {
        // Checks if the mission is a legal reporting destination for seeding this facility
        // Assumes no affiliation has been selected for a multi-affiliation card
        return _game.getRules().isLocationValidPlayCardDestinationPerRules(
                _game, this, mission, SeedCardAction.class, _ownerName, getAffiliationOptions());
    }

    public boolean canSeedAtMissionAsAffiliation(GameLocation location, Affiliation affiliation) {
        // Checks if the mission is a legal reporting destination for seeding this facility
        // Assumes an affiliation has already been selected
        return _game.getRules().isLocationValidPlayCardDestinationPerRules(
                _game, this, location, SeedCardAction.class, _ownerName, List.of(affiliation));
    }

    public Quadrant getNativeQuadrant() {
        return _blueprint.getQuadrant();
    }


    @Override
    public boolean canBeSeeded(DefaultGame game) {
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            if (canSeedAtMission(location))
                return true;
        }
        return false;
    }

    @Override
    public boolean isControlledBy(String playerId) {
        try {
            // TODO - Need to set modifiers for when cards get temporary control
            if (!_zone.isInPlay())
                return false;
            if (playerId.equals(_ownerName))
                return true;
            return getFacilityType() == FacilityType.HEADQUARTERS &&
                    _game.getGameState().getPlayer(playerId).isPlayingAffiliation(getCurrentAffiliation());
        } catch(PlayerNotFoundException exp) {
            _game.sendErrorMessage(exp);
            return false;
        }
    }

    public boolean isUsableBy(String playerId) {
        return isControlledBy(playerId);
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (hasTransporters() && isControlledBy(player.getPlayerId())) {
                actions.add(new BeamCardsAction(cardGame, player, this));
            }
            if (!Filters.filter(getAttachedCards(_game), _game, Filters.your(player), Filters.personnel).isEmpty()) {
                actions.add(new WalkCardsAction(cardGame, player, this));
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(_game));
        return actions;
    }

    public List<TopLevelSelectableAction> createSeedCardActions() {
        return List.of(new SeedOutpostAction(_game, this));
        // TODO - Add actions for non-outposts
    }


    public Collection<PhysicalCard> getDockedShips() {
        return Filters.filter(getAttachedCards(_game), _game, Filters.ship);
    }

    public Collection<PhysicalCard> getCrew() {
        return Filters.filter(getAttachedCards(_game), _game, Filters.or(Filters.personnel, Filters.equipment));
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
        return _game.getGameState().getModifiersQuerying().getAttribute(this, CardAttribute.WEAPONS);
    }

    public float getShields(DefaultGame cardGame) {
        return _game.getGameState().getModifiersQuerying().getAttribute(this, CardAttribute.SHIELDS);
    }

    public Collection<PersonnelCard> getPersonnelAboard() {
        return getPersonnelInCrew();
    }

}