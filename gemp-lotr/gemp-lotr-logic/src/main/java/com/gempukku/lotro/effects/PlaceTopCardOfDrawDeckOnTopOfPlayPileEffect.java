package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.Preventable;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.evaluator.Evaluator;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect extends AbstractEffect implements Preventable {
    private final String _playerId;
    private final Evaluator _count;
    private boolean _prevented;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(String playerId, Evaluator count) {
        _playerId = playerId;
        _count = count;
    }

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(String playerId, int count) {
        _playerId = playerId;
        _count = new ConstantEvaluator(count);
    }

    @Override
    public String getText(DefaultGame game) {
        final int cardCount = _count.evaluateExpression(game, null);
        return "Place " + cardCount + " card" + ((cardCount > 1) ? "s" : "") +
                " from top of draw deck on top of play pile";
    }
    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getGameState().getDeck(_playerId).size() >= _count.evaluateExpression(game, null);
    }

    @Override
    public String getPerformingPlayer() {
        return _playerId;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        int drawn = 0;
        int totalDraw = _count.evaluateExpression(game, null);

        while ((drawn < totalDraw) && (!_prevented) && (game.getGameState().getDeck(_playerId).size() > 0)) {
            LotroPhysicalCard card = game.getGameState().getDeck(_playerId).get(0);
            game.getGameState().removeCardsFromZone(null, Collections.singleton(card));
            game.getGameState().addCardToZone(game, card, Zone.PLAY_PILE);
            game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " from the top of their draw deck on top of their play pile");
            drawn++;
        }

        if (drawn == totalDraw) {
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
