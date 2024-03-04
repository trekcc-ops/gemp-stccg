package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.rules.TextUtils;

import java.util.*;
import java.util.stream.Stream;

public class PhysicalMissionCard extends ST1EPhysicalCard {
    private final Quadrant _quadrant;
    private final int _pointsShown;
    private final MissionType _missionType;
    private final boolean _hasNoPointBox;
    private boolean _completed = false;
    public PhysicalMissionCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _quadrant = blueprint.getQuadrant();
        _missionType = blueprint.getMissionType();
        _hasNoPointBox = blueprint.hasNoPointBox();
        _pointsShown = blueprint.getPointsShown();
    }

    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (Objects.equals(playerId, _owner.getPlayerId())) {
            return _blueprint.getOwnerAffiliationIcons();
        } else if (_blueprint.getOpponentAffiliationIcons() == null) {
            return _blueprint.getOwnerAffiliationIcons();
        } else {
            // TODO: Assumes all missions are symmetric
            return _blueprint.getOwnerAffiliationIcons();
        }
    }

    public Set<Affiliation> getAffiliationIconsForPlayer(Player player) {
        return getAffiliationIcons(player.getPlayerId());
    }

    public Quadrant getQuadrant() { return _quadrant; }
    public boolean isHomeworld() { return _blueprint.isHomeworld(); }
    @Override
    public boolean canBeSeeded() { return true; }

    public MissionType getMissionType() { return _missionType; }
    public boolean wasSeededBy(Player player) { return _owner == player; } // TODO - Does not address shared missions

    public boolean mayBeAttemptedByPlayer(Player player) {
            // Rule 7.2.1, Paragraph 1
            // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        if (_hasNoPointBox)
            return false;
        if (_completed)
            return false;
        if (wasSeededBy(player) || _pointsShown >= 40) {
            if (_missionType == MissionType.PLANET)
                return getYourAwayTeamsOnSurface(player).anyMatch(
                        awayTeam -> awayTeam.canAttemptMission(this));
            if (_missionType == MissionType.SPACE)
                return Filters.filterYourActive(player, Filters.ship, Filters.atLocation(_currentLocation))
                        .stream().anyMatch(ship -> ((PhysicalShipCard) ship).canAttemptMission(this));
        }
        return false;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        return getAwayTeamsOnSurface().filter(awayTeam -> awayTeam.getPlayer() == player);
    }

    private Stream<AwayTeam> getAwayTeamsOnSurface() {
        return getGame().getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
    }

    private Collection<PhysicalCard> getCardsOnSurface() {
        if (_missionType == MissionType.SPACE)
            return new LinkedList<>();
        else
            return getAttachedCards();
    }

    @Override
    public String getTypeSpecificCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        if (_missionType == MissionType.PLANET && _zone.isInPlay()) {
            long awayTeamCount = getAwayTeamsOnSurface().count();
            sb.append("<br><b>Away Teams on Planet</b>: ").append(awayTeamCount);
            if (awayTeamCount > 0) {
                getAwayTeamsOnSurface().forEach(awayTeam -> {
                            sb.append("<br><b>Away Team:</b> (").append(awayTeam.getPlayerId()).append(") ");
                            sb.append(TextUtils.getConcatenatedCardLinks(awayTeam.getCards()));
                        }
                        );
            }
        }
        sb.append("<br><br><b>Mission Requirements</b>: ").append(
                getMissionRequirements().replace(" OR ", " <a style='color:red'>OR</a> "));
        return sb.toString();
    }

    public String getMissionRequirements() {
        return _blueprint.getMissionRequirementsText();
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            Action action = new AttemptMissionAction(player, this);
            if (action.canBeInitiated())
                actions.add(action);
        }
        return actions;
    }

    public int getSpan(Player player) {
        if (_owner == player)
            return _blueprint.getOwnerSpan();
        else return _blueprint.getOpponentSpan();
    }

    public void isSolvedByPlayer(String playerId) {
        _game.getGameState().getPlayer(playerId).scorePoints(_blueprint.getPointsShown());
        _game.getGameState().getPlayer(playerId).addSolvedMission(this);
        _completed = true;
        _game.getGameState().checkVictoryConditions();
    }
}