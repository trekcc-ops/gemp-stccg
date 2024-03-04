package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Icon1E;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EPhysicalCard extends PhysicalCard {
    protected final ST1EGame _game;
    public ST1EPhysicalCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public ST1EGame getGame() { return _game; }

    public List<Icon1E> getIcons() { return _blueprint.getIcons(); }

}