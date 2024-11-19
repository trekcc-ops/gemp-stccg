package com.gempukku.stccg.cards;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@JsonSerialize(using = AwayTeamSerializer.class)
public class AwayTeam implements AttemptingUnit {
    private final Player _player;
    private final Collection<PhysicalReportableCard1E> _cardsInAwayTeam;
    private final ST1EGame _game;
    private final MissionLocation _location;

    public AwayTeam(ST1EGame game, Player player, MissionLocation location) {
        _player = player;
        _game = game;
        _cardsInAwayTeam = new LinkedList<>();
        _location = location;
    }


    public long getId() { return _game.getGameState().getAwayTeams().indexOf(this); }


    private boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalCard card : _cardsInAwayTeam) {
            if (card instanceof PhysicalNounCard1E noun)
                if (noun.getAffiliation() == affiliation)
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


    public int getLocationZoneIndex() { return _location.getLocationZoneIndex(); }
    public Player getPlayer() { return _player; }
    public String getPlayerId() { return _player.getPlayerId(); }
    public Collection<PhysicalReportableCard1E> getCards() { return _cardsInAwayTeam; }
    public boolean canAttemptMission(MissionLocation mission) {
        try {
            return isOnSurface(mission) && hasAnyAffiliation(mission.getAffiliationIconsForPlayer(_player));
        } catch(InvalidGameLogicException exp) {
            _game.sendErrorMessage(exp);
            return false;
        }
    }


    public void add(PhysicalReportableCard1E card) {
        _cardsInAwayTeam.add(card);
    }

    public String concatenateAwayTeam() {
        return TextUtils.concatenateStrings(_cardsInAwayTeam.stream().map(PhysicalCard::getFullName).toList());
    }

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

    public void remove(PhysicalReportableCard1E card) {
        _cardsInAwayTeam.remove(card);
        if (_cardsInAwayTeam.isEmpty())
            _game.getGameState().removeAwayTeamFromGame(this);
    }

    public boolean canBeDisbanded() {
        /* TODO - Away Teams may also be eligible to be disbanded if they're not on a mission,
            this should check presence instead. Check not sufficient in complex situations */
        List<AwayTeam> awayTeamsOnSurface = _location.getYourAwayTeamsOnSurface(_player).toList();
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

    public void disband() {
        for (PhysicalReportableCard1E card : _cardsInAwayTeam) {
            card.leaveAwayTeam();
            List<AwayTeam> awayTeamsOnSurface = _location.getYourAwayTeamsOnSurface(_player).toList();
            for (AwayTeam awayTeam : awayTeamsOnSurface) {
                if (awayTeam != this && card.getAwayTeam() == null && awayTeam.isCompatibleWith(card))
                    card.addToAwayTeam(awayTeam);
            }
        }
        assert _cardsInAwayTeam.isEmpty() :
                "Attempted to disband Away Team, but could not find a new Away Team for all cards";
    }
}