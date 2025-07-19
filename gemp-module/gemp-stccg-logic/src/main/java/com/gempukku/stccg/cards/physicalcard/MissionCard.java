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

public class MissionCard extends ST1EPhysicalCard {
    public MissionCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public int getPointsShown() { return _blueprint.getPointsShown(); }

    public boolean isHomeworld() { return _blueprint.isHomeworld(); }
    @Override
    public boolean canBeSeeded(DefaultGame game) { return true; }

    public boolean wasSeededBy(Player player) { return _owner == player; } // TODO - Does not address shared missions

    public String getMissionRequirements() {
        return _blueprint.getMissionRequirementsText();
    }

    @Override
    public List<TopLevelSelectableAction> getRulesActionsWhileInPlay(Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        if (cardGame.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            try {
                if (_currentGameLocation instanceof MissionLocation mission) {
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