package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class ST1EPhysicalCard extends PhysicalCard {
    protected final ST1EGame _game;
    public ST1EPhysicalCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public ST1EGame getGame() { return _game; }

}