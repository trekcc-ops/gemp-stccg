package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.draw.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromZoneIntoHandEffect extends DefaultEffect {
    private final PhysicalCard _card;
    private final boolean _reveal;
    private final Zone _fromZone;
    private final DefaultGame _game;

    public PutCardFromZoneIntoHandEffect(DefaultGame game, PhysicalCard card, Zone fromZone) {
        this(game, card, fromZone, true);
    }

    public PutCardFromZoneIntoHandEffect(DefaultGame game, PhysicalCard card, Zone fromZone, boolean reveal) {
        super(card);
        _card = card;
        _fromZone = fromZone;
        _reveal = reveal;
        _game = game;
    }

    @Override
    public String getText() {
        return "Put card from " + _fromZone.getHumanReadable() + " into hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return _card.getZone() == _fromZone;
    }

    public String chatMessage(PhysicalCard card) {
        String cardInfoToShow = _reveal ? card.getCardLink() : "a card";
        return card.getOwnerName() + " puts " + cardInfoToShow + " from " + _fromZone.getHumanReadable() + " into their hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            gameState.sendMessage(chatMessage(_card));
            gameState.removeCardsFromZone(_card.getOwnerName(), Collections.singleton(_card));
            gameState.addCardToZone(_card, Zone.HAND);
            _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(this, _card));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
