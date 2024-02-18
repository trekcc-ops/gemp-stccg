package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.RegularSkill;

import java.util.Collection;
import java.util.Set;

public class AwayTeam {
    private Player _player;
    private PhysicalCard _parentCard;
    private final Collection<PhysicalNounCard1E> _cardsInAwayTeam;

    public AwayTeam(Player player, PhysicalCard parentCard, Collection<PhysicalNounCard1E> cards) {
        _player = player;
        _parentCard = parentCard;
        _cardsInAwayTeam = cards;
    }

    public boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalNounCard1E card : _cardsInAwayTeam) {
            if (card.getCurrentAffiliation() == affiliation)
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

    public Player getPlayer() { return _player; }
    public String getPlayerId() { return _player.getPlayerId(); }
    public Collection<PhysicalNounCard1E> getCards() { return _cardsInAwayTeam; }
    public boolean canAttemptMission(PhysicalMissionCard missionCard) {
        return hasAffiliationFromSet(missionCard.getAffiliationIconsForPlayer(_player));
    }

    public boolean hasSkill(RegularSkill skill) {
        for (PhysicalNounCard1E card : _cardsInAwayTeam) {
/*            if (card.hasSkill(skill)) // TODO - Nothing actually checks if people have the skill here
                return true; */
        }
        return false;
    }
}
