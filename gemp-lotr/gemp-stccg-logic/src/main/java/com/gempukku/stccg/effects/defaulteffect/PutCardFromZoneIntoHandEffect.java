package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DrawCardOrPutIntoHandResult;
import com.gempukku.stccg.rules.GameUtils;

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
        return _card.getZone() == _fromZone && (_game.getFormat().doesNotHaveRuleOfFour() ||
                _game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(_game, _card.getOwnerName()));
    }

    public String chatMessage(PhysicalCard card) {
        String cardInfoToShow = _reveal ? GameUtils.getCardLink(card) : "a card";
        return card.getOwnerName() + " puts " + cardInfoToShow + " from " + _fromZone.getHumanReadable() + " into their hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            gameState.sendMessage(chatMessage(_card));
            gameState.removeCardsFromZone(_card.getOwnerName(), Collections.singleton(_card));
            gameState.addCardToZone(_game, _card, Zone.HAND);
            _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_card.getOwnerName()));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
