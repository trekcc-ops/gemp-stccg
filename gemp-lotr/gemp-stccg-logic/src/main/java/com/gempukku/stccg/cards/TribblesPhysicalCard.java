package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TribblesPlayCardAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.results.EffectResult;

public class TribblesPhysicalCard extends PhysicalCard {
    private final TribblesGame _game;
    public TribblesPhysicalCard(TribblesGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public TribblesGame getGame() { return _game; }

    @Override
    public ActionContext createActionContext(String playerId, Effect effect, EffectResult effectResult) {
        return new TribblesActionContext(playerId, getGame(), this, effect, effectResult);
    }

    public boolean canPlayOutOfSequence() {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.accepts(createActionContext()));
    }

    public Action createPlayCardAction() {
        return new TribblesPlayCardAction(this);
    }

    public boolean isNextInSequence() {
        final int cardValue = _blueprint.getTribbleValue();
        if (_game.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _game.getGameState().getNextTribbleInSequence());
    }

}