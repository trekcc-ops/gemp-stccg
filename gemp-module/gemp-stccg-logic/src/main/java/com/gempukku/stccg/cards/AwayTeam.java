package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="awayTeamId")
@JsonPropertyOrder({ "awayTeamId", "locationId", "playerId", "awayTeamCardIds" })
public class AwayTeam implements AttemptingUnit {

    @JsonProperty("playerId")
    private final String _controllerName;
    @JsonProperty("awayTeamCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<ReportableCard> _cardsInAwayTeam;
    @JsonProperty("locationId")
    private final int _locationId;
    @JsonProperty("awayTeamId")
    private final int _awayTeamId;

    public AwayTeam(String playerName, MissionLocation location, int attemptingUnitId) {
        _cardsInAwayTeam = new LinkedList<>();
        _awayTeamId = attemptingUnitId;
        _controllerName = playerName;
        _locationId = location.getLocationId();
    }


    private boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalCard card : _cardsInAwayTeam) {
            if (card instanceof AffiliatedCard affiliatedCard)
                if (affiliatedCard.isAffiliation(affiliation))
                    return true;
        }
        return false;
    }

    private boolean hasAnyAffiliation(Collection<Affiliation> affiliations) {
        return affiliations.stream().anyMatch(this::hasAffiliation);
    }
    public boolean isOnSurface(MissionLocation location) {
        return _locationId == location.getLocationId();
    }

    public boolean isOnSurface(int locationId) {
        return _locationId == locationId;
    }


    @Override
    public Collection<PersonnelCard> getAllPersonnel(DefaultGame cardGame) {
        List<PersonnelCard> result = new LinkedList<>();
        for (PhysicalCard card : getCards()) {
            if (card instanceof PersonnelCard personnel) {
                result.add(personnel);
            }
        }
        return result;
    }

    public String getControllerName() { return _controllerName; }

    @Override
    public boolean includesInAttemptingUnit(DefaultGame cardGame, PhysicalCard encounteringCard) {
        return _cardsInAwayTeam.contains(encounteringCard);
    }

    public Collection<ReportableCard> getCards() { return _cardsInAwayTeam; }

    public boolean canAttemptMission(DefaultGame cardGame, MissionLocation mission) {
        if (!isOnSurface(mission))
            return false;
        if (getAttemptingPersonnel(cardGame).isEmpty())
            return false;
        try {
            MissionCard missionCard = mission.getMissionForPlayer(_controllerName);
            if (missionCard.getBlueprint().canAnyAttempt())
                return true;
            if (missionCard.getBlueprint().canAnyExceptBorgAttempt() && !hasAffiliation(Affiliation.BORG))
                return true;
            return hasAnyAffiliation(mission.getAffiliationIcons(cardGame, _controllerName));
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }



    public void add(ReportableCard card) {
        _cardsInAwayTeam.add(card);
    }

    public String concatenateAwayTeam() {
        return TextUtils.concatenateStrings(_cardsInAwayTeam.stream().map(PhysicalCard::getFullName).toList());
    }

    public boolean isCompatibleWith(ST1EGame game, ReportableCard reportable) {
        for (ReportableCard awayTeamCard : _cardsInAwayTeam) {
            if (!awayTeamCard.isCompatibleWith(game, reportable))
                return false;
        }
        return true;
    }


    public void remove(ST1EGame cardGame, ReportableCard card) {
        _cardsInAwayTeam.remove(card);
        if (_cardsInAwayTeam.isEmpty())
            cardGame.getGameState().removeAwayTeamFromGame(this);
    }


    public boolean canBeDisbanded(ST1EGame game) {
        /* TODO - Away Teams may also be eligible to be disbanded if they're not on a mission,
            this should check presence instead. Check not sufficient in complex situations */
        Stream<AwayTeam> teamsOnSurface =
                game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(_locationId));
        List<AwayTeam> awayTeamsOnSurface =
                teamsOnSurface.filter(awayTeam -> Objects.equals(awayTeam.getControllerName(), _controllerName)).toList();

        for (ReportableCard reportable : _cardsInAwayTeam) {
            boolean canJoinAnother = false;
            for (AwayTeam awayTeam : awayTeamsOnSurface) {
                if (awayTeam != this && awayTeam.isCompatibleWith(game, reportable))
                    canJoinAnother = true;
            }
            if (!canJoinAnother)
                return false;
        }
        return true;
    }

    public void disband(ST1EGame game) {
        for (ReportableCard card : _cardsInAwayTeam) {
            remove(game, card);

            Stream<AwayTeam> teamsOnSurface =
                    game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(_locationId));
            List<AwayTeam> awayTeamsOnSurface =
                    teamsOnSurface.filter(awayTeam -> Objects.equals(awayTeam.getControllerName(), _controllerName)).toList();

            for (AwayTeam awayTeam : awayTeamsOnSurface) {
                if (awayTeam != this && game.getGameState().getAwayTeamForCard(card) == null
                        && awayTeam.isCompatibleWith(game, card)) {
                    add(card);
                }
            }
        }
        assert _cardsInAwayTeam.isEmpty() :
                "Attempted to disband Away Team, but could not find a new Away Team for all cards";
    }

    public int getAwayTeamId() {
        return _awayTeamId;
    }

    public int size() {
        return _cardsInAwayTeam.size();
    }
}