package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DrawCardOrPutIntoHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class PutCardFromZoneIntoHandEffect extends AbstractEffect<DefaultGame> {
    private final PhysicalCard _card;
    private final boolean _reveal;
    private final Zone _fromZone;

    public PutCardFromZoneIntoHandEffect(PhysicalCard card, Zone fromZone) {
        this(card, fromZone, true);
    }

    public PutCardFromZoneIntoHandEffect(PhysicalCard card, Zone fromZone, boolean reveal) {
        _card = card;
        _fromZone = fromZone;
        _reveal = reveal;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put card from " + _fromZone.getHumanReadable() + " into hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _card.getZone() == _fromZone && (game.getFormat().doesNotHaveRuleOfFour() ||
                game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, _card.getOwner()));
    }

    public String chatMessage(PhysicalCard card) {
        String cardInfoToShow = _reveal ? GameUtils.getCardLink(card) : "a card";
        return card.getOwner() + " puts " + cardInfoToShow + " from " + _fromZone.getHumanReadable() + " into their hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(chatMessage(_card));
            gameState.removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            gameState.addCardToZone(game, _card, Zone.HAND);
            game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_card.getOwner()));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
