package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class PhysicalCardImpl extends PhysicalCard {
    private final DefaultGame _game;

    public PhysicalCardImpl(DefaultGame game, int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, game.getGameState().getPlayer(owner), blueprint);
        _game = game;
    }

    public PhysicalCardImpl(DefaultGame game, int cardId, String blueprintId, Player owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}