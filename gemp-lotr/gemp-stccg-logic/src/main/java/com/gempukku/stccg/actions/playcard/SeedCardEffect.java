package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedCardEffect extends DefaultEffect {
    protected final Zone _fromZone;
    protected final Zone _seedToZone;
    protected final PhysicalCard _cardSeeded;
    protected final ST1EGame _game;

    public SeedCardEffect(String performingPlayerId, ST1EPhysicalCard cardSeeded, Zone seedToZone) {
        super(performingPlayerId);
        _fromZone = cardSeeded.getZone();
        _cardSeeded = cardSeeded;
        _game = cardSeeded.getGame();
        _seedToZone = seedToZone;
    }

    public PhysicalCard getPlayedCard() {
        return _cardSeeded;
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
        ST1EGameState gameState = _game.getGameState();

        gameState.sendMessage(_cardSeeded.getOwnerName() + " seeded " + _cardSeeded.getCardLink());
        gameState.removeCardFromZone(_cardSeeded);
        if (_fromZone == Zone.DRAW_DECK) {
            _game.getGameState().sendMessage(_cardSeeded.getOwnerName() + " shuffles their deck");
            _game.getGameState().shuffleDeck(_cardSeeded.getOwnerName());
        }
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        _game.getGameState().addCardToZone(_cardSeeded, _seedToZone);
        _game.getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _fromZone, _cardSeeded));

        return new FullEffectResult(true);
    }
}