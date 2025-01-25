package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.WalkCardsAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FacilityCard extends PhysicalNounCard1E implements AffiliatedCard, CardWithCrew {
    public FacilityCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }
    public FacilityType getFacilityType() {
        return getBlueprint().getFacilityType();
    }

    public boolean canSeedAtMission(MissionLocation mission) {
        for (Affiliation affiliation : getAffiliationOptions())
            if (canSeedAtMissionAsAffiliation(mission, affiliation))
                return true;
        return false;
    }

    public boolean canSeedAtMissionAsAffiliation(MissionLocation mission, Affiliation affiliation) {
        try {
            if (mission.isHomeworld())
                return false;
            if (mission.hasFacilityOwnedByPlayer(_owner.getPlayerId()))
                return false;
            return mission.getAffiliationIcons(_owner.getPlayerId()).contains(affiliation) &&
                    mission.getQuadrant() == getNativeQuadrant();
        } catch(InvalidGameLogicException exp) {
            _game.sendErrorMessage(exp);
            return false;
        }
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
        // TODO - Need to set modifiers for when cards get temporary control
        if (!_zone.isInPlay())
            return false;
        if (playerId.equals(_owner.getPlayerId()))
            return true;
        return getFacilityType() == FacilityType.HEADQUARTERS &&
                _game.getGameState().getPlayer(playerId).isPlayingAffiliation(getCurrentAffiliation());
    }

    public boolean isUsableBy(String playerId) {
        return isControlledBy(playerId);
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (hasTransporters() && isControlledBy(player.getPlayerId())) {
                actions.add(new BeamCardsAction(player, this));
            }
            if (!Filters.filter(getAttachedCards(_game), Filters.your(player), Filters.personnel).isEmpty()) {
                actions.add(new WalkCardsAction(player, this));
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(player.getGame()));
        return actions;
    }

    public TopLevelSelectableAction createSeedCardAction() {
        return new SeedOutpostAction(this);
        // TODO - Add actions for non-outposts
    }

    public Collection<PhysicalCard> getDockedShips() {
        return Filters.filter(getAttachedCards(_game), Filters.ship);
    }

    public Collection<PhysicalCard> getCrew() {
        return Filters.filter(getAttachedCards(_game), Filters.or(Filters.personnel, Filters.equipment));
    }

}