package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.WalkCardsAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
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

    public boolean canSeedAtMission(DefaultGame cardGame, MissionLocation mission) {
        for (Affiliation affiliation : getAffiliationOptions())
            if (canSeedAtMissionAsAffiliation(cardGame, mission, affiliation))
                return true;
        return false;
    }


    public boolean canSeedAtMissionAsAffiliation(DefaultGame cardGame, MissionLocation mission,
                                                 Affiliation affiliation) {
        try {
            if (mission.isHomeworld())
                return false;

            Collection<PhysicalCard> facilitiesOwnedByPlayerHere =
                    Filters.filterYourActive(cardGame, _owner, CardType.FACILITY, Filters.atLocation(mission));

            if (!facilitiesOwnedByPlayerHere.isEmpty())
                return false;
            return mission.getAffiliationIcons(_owner.getPlayerId()).contains(affiliation) &&
                    mission.getQuadrant() == getNativeQuadrant();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }



    @Override
    public boolean canBeSeeded(DefaultGame game) {
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            if (canSeedAtMission(game, location))
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
            if (playerId.equals(_owner.getPlayerId()))
                return true;
            return getFacilityType() == FacilityType.HEADQUARTERS &&
                    _game.getGameState().getPlayer(playerId).isPlayingAffiliation(getAffiliation());
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
                actions.add(new BeamCardsAction(player, this));
            }
            if (!Filters.filter(getAttachedCards(_game), Filters.your(player), Filters.personnel).isEmpty()) {
                actions.add(new WalkCardsAction(player, this));
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(_game));
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