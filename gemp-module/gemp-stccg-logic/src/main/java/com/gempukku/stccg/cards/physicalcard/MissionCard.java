package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.battle.ShipBattleAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;

import java.util.*;
import java.util.stream.Stream;

public class MissionCard extends ST1EPhysicalCard {
    protected boolean _completed = false;
    public MissionCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (Objects.equals(playerId, _owner.getPlayerId())) {
            return _blueprint.getOwnerAffiliationIcons();
        } else if (_blueprint.getOpponentAffiliationIcons() == null) {
            return _blueprint.getOwnerAffiliationIcons();
        } else {
            return _blueprint.getOwnerAffiliationIcons();
        }
    }

    private int getPointsShown() { return _blueprint.getPointsShown(); }

    public Set<Affiliation> getAffiliationIconsForPlayer(Player player) {
        return getAffiliationIcons(player.getPlayerId());
    }

    public boolean isHomeworld() { return _blueprint.isHomeworld(); }
    @Override
    public boolean canBeSeeded(DefaultGame game) { return true; }

    public boolean wasSeededBy(Player player) { return _owner == player; } // TODO - Does not address shared missions
    private boolean hasNoPointBox() { return _blueprint.hasNoPointBox(); }

    MissionType getMissionType() { return _blueprint.getMissionType(); }

    public boolean mayBeAttemptedByPlayer(Player player) {
            // Rule 7.2.1, Paragraph 1
            // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        if (hasNoPointBox())
            return false;
        if (_completed)
            return false;
        if (wasSeededBy(player) || getPointsShown() >= 40) {
            if (getMissionType() == MissionType.PLANET)
                return getYourAwayTeamsOnSurface(player).anyMatch(
                        awayTeam -> awayTeam.canAttemptMission(this));
            if (getMissionType() == MissionType.SPACE)
                return Filters.filterYourActive(player, Filters.ship, Filters.atLocation(_currentLocation))
                        .stream().anyMatch(ship -> ((PhysicalShipCard) ship).canAttemptMission(this));
        }
        return false;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        return getAwayTeamsOnSurface().filter(awayTeam -> awayTeam.getPlayer() == player);
    }
    public Stream<AwayTeam> getAwayTeamsOnSurface() {
        return _game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
    }

    public String getMissionRequirements() {
        return _blueprint.getMissionRequirementsText();
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            actions.add(new AttemptMissionAction(player, this));
            try {
                actions.add(new ShipBattleAction(this, player, this.getLocation()));
            } catch(InvalidGameLogicException exp) {
                _game.sendErrorMessage(exp);
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(player.getGame()));
        return actions;
    }

    public int getSpan(Player player) {
        if (_owner == player)
            return _blueprint.getOwnerSpan();
        else return _blueprint.getOpponentSpan();
    }

    public int getPoints() { return _blueprint.getPointsShown(); }

    public boolean isCompleted() { return _completed; }

    public void setCompleted(boolean completed) { _completed = completed; }

    @Override
    public MissionCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        MissionCard newCard = new MissionCard(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);

        for (PhysicalCard card : _cardsSeededUnderneath)
            newCard.addCardToSeededUnder(snapshotData.getDataForSnapshot(card));

        for (Map.Entry<Player, List<PhysicalCard>> entry : _cardsPreSeededUnderneath.entrySet())
            for (PhysicalCard card : entry.getValue())
                newCard.addCardToPreSeeds(snapshotData.getDataForSnapshot(card), entry.getKey());

        newCard._completed = _completed;

        return newCard;
    }

    public MissionRequirement getRequirements() {
        return _blueprint.getMissionRequirements();
    }
}