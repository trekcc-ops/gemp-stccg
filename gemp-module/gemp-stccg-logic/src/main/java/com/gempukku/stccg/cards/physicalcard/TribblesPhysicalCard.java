package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class TribblesPhysicalCard extends AbstractPhysicalCard<TribblesGame> {
    private final TribblesGame _game;
    public TribblesPhysicalCard(TribblesGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public TribblesGame getGame() { return _game; }

    public boolean isMisSeed(TribblesGame game, MissionLocation mission) {
        return false;
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(boolean forFree) { return new TribblesPlayCardAction(this); }

    public boolean canPlayOutOfSequence(TribblesGame cardGame) {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.accepts(createActionContext(cardGame)));
    }

    public boolean isNextInSequence(TribblesGame cardGame) {
        final int cardValue = _blueprint.getTribbleValue();
        if (cardGame.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == cardGame.getGameState().getNextTribbleInSequence());
    }

}