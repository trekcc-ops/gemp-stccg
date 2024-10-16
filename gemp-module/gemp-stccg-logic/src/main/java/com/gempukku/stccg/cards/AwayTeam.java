package com.gempukku.stccg.cards;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EMission;

import java.util.*;

public class AwayTeam implements AttemptingUnit {
    private final Player _player;
    private final ST1EPhysicalCard _parentCard;
    private final Collection<PhysicalReportableCard1E> _cardsInAwayTeam;

    public AwayTeam(Player player, ST1EMission mission) {
        _player = player;

        // TODO - Uses card instead of Mission. Prevents Away Teams attached to separate cards at a shared mission.
        _parentCard = mission.getInitialMissionCard();

        _cardsInAwayTeam = new LinkedList<>();
        _parentCard.getGame().getGameState().addAwayTeamToGame(this);
    }

    public boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalCard card : _cardsInAwayTeam) {
            if (card instanceof PhysicalNounCard1E noun)
                if (noun.getAffiliation() == affiliation)
                    return true;
        }
        return false;
    }

    public boolean hasAffiliationFromSet(Set<Affiliation> affiliations) {
        return affiliations.stream().anyMatch(this::hasAffiliation);
    }

    public boolean isOnSurface(PhysicalCard planet) {
        return _parentCard == planet;
    }

    public boolean isOnSurface(ST1EMission mission) {
        return Objects.equals(mission, getMission());
    }


    public Player getPlayer() { return _player; }
    public String getPlayerId() { return _player.getPlayerId(); }
    public Collection<PhysicalReportableCard1E> getCards() { return _cardsInAwayTeam; }

    public boolean canAttemptMission(ST1EMission mission) {
        return isOnSurface(mission) && hasAffiliationFromSet(mission.getAffiliationIcons(_player));
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
            card.getGame().getGameState().removeAwayTeamFromGame(this);
    }

    public boolean canBeDisbanded() {
        /* TODO - Away Teams may also be eligible to be disbanded if they're not on a mission,
            this should check presence instead. Check not sufficient in complex situations */
        ST1EMission mission = getMission();
        if (mission == null)
            return true;
        List<AwayTeam> awayTeamsOnSurface = mission.getYourAwayTeamsOnSurface(_player).toList();
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
        ST1EMission mission = getMission();
        if (mission != null) {
            for (PhysicalReportableCard1E card : _cardsInAwayTeam) {
                card.leaveAwayTeam();
                List<AwayTeam> awayTeamsOnSurface = mission.getYourAwayTeamsOnSurface(_player).toList();
                for (AwayTeam awayTeam : awayTeamsOnSurface) {
                    if (awayTeam != this && card.getAwayTeam() == null && awayTeam.isCompatibleWith(card))
                        card.addToAwayTeam(awayTeam);
                }
            }
            assert _cardsInAwayTeam.isEmpty() :
                    "Attempted to disband Away Team, but could not find a new Away Team for all cards";
        }
    }

    public ST1EMission getMission() {
        if (_parentCard instanceof MissionCard missionCard)
            return missionCard.getMission();
        else return null; // TODO - NPE risk
    }
}