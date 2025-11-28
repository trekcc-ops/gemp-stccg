package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class YouPlayerResolver {

    private final PhysicalCard _gameTextCard;

    public YouPlayerResolver(PhysicalCard gameTextCard) {
        _gameTextCard = gameTextCard;
    }

    public String getPlayerName() {
        return _gameTextCard.getControllerName();
    }

}