package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class StackCardFromDiscardEffect extends DefaultEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;
    private final DefaultGame _game;

    public StackCardFromDiscardEffect(DefaultGame game, PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _card.getZone() == Zone.DISCARD && _stackOn.getZone().isInPlay();
    }

    @Override
    public String getText() {
        return "Stack " + _card.getFullName() + " from discard on " + _stackOn.getFullName();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(_card.getOwner() + " stacks " + GameUtils.getCardLink(_card) + " from discard on " + GameUtils.getCardLink(_stackOn));
            _game.getGameState().removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            _game.getGameState().stackCard(_game, _card, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
