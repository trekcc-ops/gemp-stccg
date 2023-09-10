package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.Preventable;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect extends AbstractEffect implements Preventable {
    private final String _playerId;
    private boolean _prevented;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Place top card of draw deck on top of play pile";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getGameState().getDeck(_playerId).size() >= 1;
    }

    @Override
    public String getPerformingPlayer() {
        return _playerId;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        int drawn = 0;
        if (!_prevented && game.getGameState().getDeck(_playerId).size() > 0) {
            LotroPhysicalCard card = game.getGameState().getDeck(_playerId).get(0);
            game.getGameState().removeCardsFromZone(null, Collections.singleton(card));
            game.getGameState().addCardToZone(game, card, Zone.PLAY_PILE);
            game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " from the top of their draw deck on top of their play pile");
            drawn++;
        }

        if (drawn == 1) {
            return new FullEffectResult(true);
        } else
            return new FullEffectResult(false);
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPrevented(DefaultGame game) {
        return _prevented;
    }
}
