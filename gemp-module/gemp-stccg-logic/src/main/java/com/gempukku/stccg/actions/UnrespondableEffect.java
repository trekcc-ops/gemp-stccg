package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public abstract class UnrespondableEffect extends DefaultEffect {

    protected UnrespondableEffect(ActionContext context) {
        this(context.getGame());
    }

    protected UnrespondableEffect(DefaultGame game) {
        super(game,"none"); // TODO - This isn't right but these don't seem like effects with players
    }
    protected UnrespondableEffect(Player player) {
        super(player);
    }

    protected UnrespondableEffect(DefaultGame game, String playerId) {
        super(game, playerId);
    }

    protected abstract void doPlayEffect() throws InvalidCardDefinitionException, InvalidGameLogicException;

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        try {
            doPlayEffect();
            return new FullEffectResult(true);
        } catch(InvalidGameLogicException | InvalidCardDefinitionException exp) {
            return new FullEffectResult(false);
        }
    }
}