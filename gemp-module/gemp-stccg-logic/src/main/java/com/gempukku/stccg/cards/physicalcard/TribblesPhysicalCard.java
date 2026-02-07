package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public class TribblesPhysicalCard extends AbstractPhysicalCard {

    public TribblesPhysicalCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }
    public TribblesPhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
    }

    public boolean isMisSeed(DefaultGame game, MissionLocation mission) {
        return false;
    }

    @Override
    public List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction,
                                            AttemptingUnit attemptingUnit, MissionLocation missionLocation) {
        return new LinkedList<>();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame, boolean forFree) {
        return new TribblesPlayCardAction(cardGame, this);
    }

    public boolean canPlayOutOfSequence(TribblesGame cardGame) {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.isTrue(this, cardGame));
    }

    public boolean isNextInSequence(TribblesGame cardGame) {
        final int cardValue = _blueprint.getTribbleValue();
        if (cardGame.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == cardGame.getGameState().getNextTribbleInSequence());
    }

}