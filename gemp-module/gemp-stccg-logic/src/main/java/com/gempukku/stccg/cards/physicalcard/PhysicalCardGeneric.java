package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class PhysicalCardGeneric extends PhysicalCard {
    private final DefaultGame _game;

    public PhysicalCardGeneric(DefaultGame game, int cardId, String owner, CardBlueprint blueprint) {
        super(cardId, game.getGameState().getPlayer(owner), blueprint);
        _game = game;
    }

    public PhysicalCardGeneric(DefaultGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }

    @Override
    public CostToEffectAction getPlayCardAction(boolean forFree) {
        return new PlayCardAction(this) {
          @Override
          public DefaultGame getGame() { return _game; }
        };
    }


}