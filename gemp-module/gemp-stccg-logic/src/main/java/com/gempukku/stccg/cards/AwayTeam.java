package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="awayTeamId")
@JsonPropertyOrder({ "awayTeamId", "locationId", "playerId", "awayTeamCardIds" })
public class AwayTeam implements AttemptingUnit {
    @JsonProperty("playerId")
    @JsonIdentityReference(alwaysAsId=true)
    private final Player _player;
    @JsonProperty("awayTeamCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalReportableCard1E> _cardsInAwayTeam;
    @JsonProperty("locationId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionLocation _location;
    @JsonProperty("awayTeamId")
    private final int _awayTeamId;

    public AwayTeam(Player player, MissionLocation location, int attemptingUnitId) {
        _player = player;
        _cardsInAwayTeam = new LinkedList<>();
        _location = location;
        _awayTeamId = attemptingUnitId;
    }


    private boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalCard card : _cardsInAwayTeam) {
            if (card instanceof AffiliatedCard affiliatedCard)
                if (affiliatedCard.getCurrentAffiliation() == affiliation)
                    return true;
        }
        return false;
    }

    private boolean hasAnyAffiliation(Collection<Affiliation> affiliations) {
        return affiliations.stream().anyMatch(this::hasAffiliation);
    }
    public boolean isOnSurface(MissionLocation location) {
        return _location == location;
    }

    @JsonIgnore
    public Player getPlayer() { return _player; }
    public String getPlayerId() { return _player.getPlayerId(); }
    public Collection<PhysicalReportableCard1E> getCards() { return _cardsInAwayTeam; }

    public boolean canAttemptMission(DefaultGame cardGame, MissionLocation mission) {
        if (!isOnSurface(mission))
            return false;
        if (getAttemptingPersonnel().isEmpty())
            return false;
        try {
            MissionCard missionCard = mission.getMissionForPlayer(_player.getPlayerId());
            if (missionCard.getBlueprint().canAnyAttempt())
                return true;
            if (missionCard.getBlueprint().canAnyExceptBorgAttempt() && !hasAffiliation(Affiliation.BORG))
                return true;
            return hasAnyAffiliation(mission.getAffiliationIconsForPlayer(_player));
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }



    public void add(PhysicalReportableCard1E card) {
        _cardsInAwayTeam.add(card);
    }

    public String concatenateAwayTeam() {
        return TextUtils.concatenateStrings(_cardsInAwayTeam.stream().map(PhysicalCard::getFullName).toList());
    }

    @JsonIgnore
    public Collection<PersonnelCard> getAllPersonnel() {
        List<PersonnelCard> result = new LinkedList<>();
        for (PhysicalCard card : getCards()) {
            if (card instanceof PersonnelCard personnel) {
                result.add(personnel);
            }
        }
        return result;
    }

    public boolean isCompatibleWith(PhysicalReportableCard1E reportable) {
        for (PhysicalReportableCard1E awayTeamCard : _cardsInAwayTeam) {
            if (!awayTeamCard.isCompatibleWith(reportable))
                return false;
        }
        return true;
    }

    public void remove(ST1EGame cardGame, PhysicalReportableCard1E card) {
        _cardsInAwayTeam.remove(card);
        if (_cardsInAwayTeam.isEmpty())
            cardGame.getGameState().removeAwayTeamFromGame(this);
    }


    public boolean canBeDisbanded(ST1EGame game) {
        /* TODO - Away Teams may also be eligible to be disbanded if they're not on a mission,
            this should check presence instead. Check not sufficient in complex situations */
        List<AwayTeam> awayTeamsOnSurface = _location.getYourAwayTeamsOnSurface(game, _player).toList();
        for (PhysicalReportableCard1E reportable : _cardsInAwayTeam) {
            boolean canJoinAnother = false;
            for (AwayTeam awayTeam : awayTeamsOnSurface) {
                if (awayTeam != this && awayTeam.isCompatibleWith(reportable))
                    canJoinAnother = true;
            }
            if (!canJoinAnother)
                return false;
        }
        return true;
    }

    public void disband(ST1EGame game) {
        for (PhysicalReportableCard1E card : _cardsInAwayTeam) {
            card.leaveAwayTeam(game);
            List<AwayTeam> awayTeamsOnSurface = _location.getYourAwayTeamsOnSurface(game, _player).toList();
            for (AwayTeam awayTeam : awayTeamsOnSurface) {
                if (awayTeam != this && card.getAwayTeam() == null && awayTeam.isCompatibleWith(card))
                    card.addToAwayTeam(awayTeam);
            }
        }
        assert _cardsInAwayTeam.isEmpty() :
                "Attempted to disband Away Team, but could not find a new Away Team for all cards";
    }
}