package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.BeamCardsAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.AwayTeam;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.rules.GameUtils;

import java.util.*;

import static com.gempukku.stccg.filters.Filters.ship;

public class PhysicalMissionCard extends PhysicalCard {
    private Quadrant _quadrant;
    private ST1ELocation _location = null;
    private final ST1EGame _game;
    private final int _pointsShown;
    private MissionType _missionType;
    private final boolean _hasNoPointBox;
    public PhysicalMissionCard(ST1EGame game, int cardId, String blueprintId, Player owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _quadrant = blueprint.getQuadrant();
        _game = game;
        _missionType = blueprint.getMissionType();
        _hasNoPointBox = blueprint.hasNoPointBox();
        _pointsShown = blueprint.getPointsShown();
    }

    @Override
    public ST1EGame getGame() { return _game; }

    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (Objects.equals(playerId, _ownerName)) {
            return _blueprint.getOwnerAffiliationIcons();
        } else if (_blueprint.getOpponentAffiliationIcons() == null) {
            return _blueprint.getOwnerAffiliationIcons();
        } else {
            // TODO: Assumes all missions are symmetric
            return _blueprint.getOwnerAffiliationIcons();
        }
    }
    public Quadrant getQuadrant() { return _quadrant; }
    public boolean isHomeworld() { return _blueprint.isHomeworld(); }
    @Override
    public boolean canBeSeeded() { return true; }
    public void setLocation(ST1ELocation location) { _location = location; }
    @Override
    public ST1ELocation getLocation() { return _location; }
    public boolean wasSeededBy(Player player) { return _owner == player; } // TODO - Does not address shared missions

    public boolean mayBeAttemptedByPlayer(Player player) {
            // Rule 7.2.1, Paragraph 1
            // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        if (_hasNoPointBox)
            return false;
        if (wasSeededBy(player) || _pointsShown >= 40) {
            if (_missionType == MissionType.PLANET)
                for (AwayTeam awayTeam : getAwayTeamsOnSurface())
                    for (Affiliation affiliation : getAffiliationIcons(player.getPlayerId()))
                        if (awayTeam.hasAffiliation(affiliation))
                            return true;
                    // TODO - Implement for space missions
/*            else if (_missionType == MissionType.SPACE)
                for (PhysicalShipCard ship : Filters.filterActive(
                    if (ship.isUndocked() && ship.canAttemptMission(this))
                        return true; */
        }
        return false;
    }

    public void organizeAwayTeamsOnSurface() {
           // TODO - Very simplistic implementation. Ignores compatibility.
        if (_missionType == MissionType.PLANET) {
            Set<AwayTeam> awayTeams = getGame().getGameState().getAwayTeams();
                // Disband existing Away Teams on surface
            awayTeams.removeIf(awayTeam -> awayTeam.isOnSurface(this));
            for (Player player : getGame().getPlayers()) {
                Set<PhysicalNounCard1E> awayTeamCards = new HashSet<>();
                for (PhysicalCard card : getCardsOnSurface()) {
                    if (card.isControlledBy(player.getPlayerId()) && card instanceof PhysicalNounCard1E) {
                        awayTeamCards.add((PhysicalNounCard1E) card);
                    }
                }
                if (!awayTeamCards.isEmpty()) {
                    awayTeams.add(new AwayTeam(player, this, awayTeamCards));
                }
            }
        }
    }

    private Collection<AwayTeam> getAwayTeamsOnSurface() {
        Set<AwayTeam> awayTeamsOnSurface = new HashSet<>();
        for (AwayTeam awayTeam : getGame().getGameState().getAwayTeams()) {
            if (awayTeam.isOnSurface(this)) {
                awayTeamsOnSurface.add(awayTeam);
            }
        }
        return awayTeamsOnSurface;
    }

    private Collection<PhysicalCard> getCardsOnSurface() {
        if (_missionType == MissionType.SPACE)
            return new LinkedList<>();
        else
            return getAttachedCards();
    }

    @Override
    public String getTypeSpecificCardInfoHTML() {
        if (_missionType == MissionType.PLANET) {
            StringBuilder sb = new StringBuilder();
            sb.append("<br><b>Away Teams on Planet</b>: ").append(getAwayTeamsOnSurface().size());
            if (!getAwayTeamsOnSurface().isEmpty()) {
                for (AwayTeam awayTeam : getAwayTeamsOnSurface()) {
                    sb.append("<br><b>Away Team:</b> (").append(awayTeam.getPlayerId()).append(") ");
                    sb.append(GameUtils.getAppendedNames(awayTeam.getCards()));
                }
            }
            return sb.toString();
        }
        else return "";
    }

        // TODO - Initiating mission attempts
/*    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (mayBeAttemptedByPlayer(player))
                actions.add(new AttemptMissionAction(player, this));
        }
        return actions;
    } */
}