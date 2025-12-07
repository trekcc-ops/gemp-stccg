package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MissionCard extends ST1EPhysicalCard {

    public MissionCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }


    public int getPointsShown() { return _blueprint.getPointsShown(); }

    public boolean isHomeworld() { return _blueprint.isHomeworld(); }

    public boolean wasSeededBy(Player player) {
        return Objects.equals(_ownerName, player.getPlayerId());
    } // TODO - Does not address shared missions

    public String getMissionRequirements() {
        return _blueprint.getMissionRequirementsText();
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (cardGame.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            try {
                if (cardGame instanceof ST1EGame stGame &&
                        stGame.getGameState().getLocationById(_currentLocationId) instanceof MissionLocation mission) {
                    actions.add(new AttemptMissionAction(
                            cardGame, player, mission.getCardForActionSelection(player), mission));
                }
            } catch (InvalidGameLogicException exp) {
                cardGame.sendErrorMessage(exp);
            }
        }
        actions.removeIf(action -> !action.canBeInitiated(cardGame));
        return actions;
    }

    public int getPoints() { return _blueprint.getPointsShown(); }

    public boolean hasAffiliationIconForOwner(Affiliation affiliation) {
        return _blueprint.getOwnerAffiliationIcons().contains(affiliation);
    }
}