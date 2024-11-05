package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;

public class SeedCardEffect extends DefaultEffect {
    protected final Zone _fromZone;
    protected final Zone _seedToZone;
    protected final PhysicalCard _cardSeeded;
    protected final ST1EGame _st1eGame; // TODO - redundant variable with _game in DefaultEffect
    protected final Action _causalAction;

    public SeedCardEffect(String performingPlayerId, ST1EPhysicalCard cardSeeded, Zone seedToZone, Action action) {
        super(cardSeeded.getGame(), performingPlayerId);
        _fromZone = cardSeeded.getZone();
        _cardSeeded = cardSeeded;
        _seedToZone = seedToZone;
        _st1eGame = cardSeeded.getGame();
        _causalAction = action;
    }

    @Override
    public String getText() {
        return "Seed " + _cardSeeded.getFullName();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        GameState gameState = getGame().getGameState();

        getGame().sendMessage(_cardSeeded.getOwnerName() + " seeded " + _cardSeeded.getCardLink());
        gameState.removeCardFromZone(_cardSeeded);
        if (_fromZone == Zone.DRAW_DECK) {
            getGame().sendMessage(_cardSeeded.getOwnerName() + " shuffles their deck");
            getGame().getGameState().shuffleDeck(_cardSeeded.getOwnerName());
        }
        _game.getActionsEnvironment().addPerformedAction(_causalAction);
        getGame().getGameState().addCardToZone(_cardSeeded, _seedToZone);
        getGame().getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _fromZone, _cardSeeded));

        return new FullEffectResult(true);
    }

    @Override
    public ST1EGame getGame() { return _st1eGame; }
}