package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.battle.ShipBattleAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class MissionCard extends ST1EPhysicalCard {
    private final Quadrant _quadrant;
    private final int _pointsShown;
    private final MissionType _missionType;
    private final boolean _hasNoPointBox;
    protected boolean _completed = false;
    public MissionCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
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

    @Override
    public String getCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getCardInfoHTML());
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
        if (_cardsSeededUnderneath.size() > 0) {
            sb.append("<br><br><b>Cards seeded underneath</b>:");
            sb.append("<ol>");
            for (PhysicalCard card : _cardsSeededUnderneath) {
                sb.append("<li>" + card.getTitle() + "</li>");
            }
            sb.append("</ol>");
        }
        return sb.toString();
    }

    public String getMissionRequirements() {
        return _blueprint.getMissionRequirementsText();
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            actions.add(new AttemptMissionAction(player, this));
            actions.add(new ShipBattleAction(this, player, this.getLocation()));
        }
        actions.removeIf(action -> !action.canBeInitiated());
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