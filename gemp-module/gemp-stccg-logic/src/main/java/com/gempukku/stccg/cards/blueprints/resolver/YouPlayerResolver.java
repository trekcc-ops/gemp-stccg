package com.gempukku.stccg.cards.blueprints.resolver;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.Player;

public class YouPlayerResolver {

    private final PhysicalCard _gameTextCard;

    public YouPlayerResolver(PhysicalCard gameTextCard) {
        _gameTextCard = gameTextCard;
    }

    public Player getPlayer() {
        return _gameTextCard.getController();
    }

}