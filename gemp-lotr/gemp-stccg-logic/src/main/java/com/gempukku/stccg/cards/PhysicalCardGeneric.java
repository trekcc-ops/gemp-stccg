package com.gempukku.stccg.cards;

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


}