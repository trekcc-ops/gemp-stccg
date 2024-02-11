package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;

public class PhysicalCardImpl extends PhysicalCard {
    private final DefaultGame _game;
    public PhysicalCardImpl(int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        this(null, cardId, blueprintId, owner, blueprint);
    }

    public PhysicalCardImpl(DefaultGame game, int cardId, String blueprintId, String owner, CardBlueprint blueprint) {
        super(cardId, blueprintId, owner, blueprint);
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}