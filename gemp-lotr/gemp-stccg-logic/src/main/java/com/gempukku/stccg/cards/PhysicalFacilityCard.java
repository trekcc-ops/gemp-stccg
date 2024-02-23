package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.BeamCardsAction;
import com.gempukku.stccg.actions.SeedOutpostAction;
import com.gempukku.stccg.actions.WalkCardsAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.rules.GameUtils;

import java.util.*;

public class PhysicalFacilityCard extends PhysicalNounCard1E {
    private final Set<PhysicalCard> _cardsAboard = new HashSet<>();
    public PhysicalFacilityCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }
    public FacilityType getFacilityType() {
        return getBlueprint().getFacilityType();
    }
    public boolean canSeedAtMission(PhysicalMissionCard mission) {
        for (Affiliation affiliation : _affiliationOptions)
            if (canSeedAtMissionAsAffiliation(mission, affiliation))
                return true;
        return false;
    }
    public boolean canSeedAtMissionAsAffiliation(PhysicalMissionCard mission, Affiliation affiliation) {
        if (mission.isHomeworld())
            return false;
        if (mission.getLocation().hasFacilityOwnedByPlayer(_ownerName))
            return false;
        return mission.getAffiliationIcons(_ownerName).contains(affiliation) && mission.getQuadrant() == _nativeQuadrant;
    }

    @Override
    public boolean canBeSeeded() {
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            for (PhysicalMissionCard mission : location.getMissions())
                if (this.canSeedAtMission(mission))
                    return true;
        }
        return false;
    }

    @Override
    public boolean isControlledBy(String playerId) {
        if (Objects.equals(_cardController, playerId))
            return true;
        return getFacilityType() == FacilityType.HEADQUARTERS &&
                _game.getGameState().getPlayer(playerId).isPlayingAffiliation(getCurrentAffiliation());
    }

    public boolean isUsableBy(String playerId) {
        return isControlledBy(playerId);
    }

    public void addCardAboard(PhysicalCard card) {
        _cardsAboard.add(card);
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (hasTransporters() && isControlledBy(player.getPlayerId())) {
                actions.add(new BeamCardsAction(player, this));
            }
            if (!Filters.filter(getAttachedCards(), Filters.your(player), Filters.personnel).isEmpty()) {
                actions.add(new WalkCardsAction(player, this));
            }
        }
        actions.removeIf(action -> !action.canBeInitiated());
        return actions;
    }

    public Action createSeedCardAction() {
        return new SeedOutpostAction(this);
        // TODO - Add actions for non-outposts
    }

    public boolean allowsDocking() { return true; } // TODO - not always true

    Collection<PhysicalCard> getDockedShips() {
        return Filters.filter(getAttachedCards(), Filters.ship);
    }

    Collection<PhysicalCard> getCrew() {
        return Filters.filter(getAttachedCards(), Filters.or(Filters.personnel, Filters.equipment));
    }


    public String getTypeSpecificCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Docked ships",getDockedShips());
        attachedCards.put("Crew",getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty())
                sb.append("<br><b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ").append(GameUtils.getConcatenatedCardLinks(entry.getValue())).append("<br>");
        }
        return sb.toString();
    }
}