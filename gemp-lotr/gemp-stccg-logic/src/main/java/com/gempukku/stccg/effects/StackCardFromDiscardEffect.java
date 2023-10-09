package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class StackCardFromDiscardEffect extends AbstractEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;

    public StackCardFromDiscardEffect(PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _card.getZone() == Zone.DISCARD && _stackOn.getZone().isInPlay();
    }

    @Override
    public String getText(DefaultGame game) {
        return "Stack " + GameUtils.getFullName(_card) + " from discard on " + GameUtils.getFullName(_stackOn);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(_card.getOwner() + " stacks " + GameUtils.getCardLink(_card) + " from discard on " + GameUtils.getCardLink(_stackOn));
            game.getGameState().removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            game.getGameState().stackCard(game, _card, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
