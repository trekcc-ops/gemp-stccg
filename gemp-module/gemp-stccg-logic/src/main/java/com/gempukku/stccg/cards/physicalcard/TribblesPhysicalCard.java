package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class TribblesPhysicalCard extends AbstractPhysicalCard {
    private final TribblesGame _game;
    public TribblesPhysicalCard(TribblesGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public TribblesGame getGame() { return _game; }

    public boolean isMisSeed(DefaultGame game, MissionLocation mission) {
        return false;
    }

    @Override
    public List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction, AttemptingUnit attemptingUnit, MissionLocation missionLocation) throws InvalidGameLogicException, PlayerNotFoundException {
        return new LinkedList<>();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(boolean forFree) { return new TribblesPlayCardAction(this); }

    public boolean canPlayOutOfSequence(TribblesGame cardGame) {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.accepts(createActionContext(), cardGame));
    }

    public boolean isNextInSequence(TribblesGame cardGame) {
        final int cardValue = _blueprint.getTribbleValue();
        if (cardGame.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == cardGame.getGameState().getNextTribbleInSequence());
    }

}