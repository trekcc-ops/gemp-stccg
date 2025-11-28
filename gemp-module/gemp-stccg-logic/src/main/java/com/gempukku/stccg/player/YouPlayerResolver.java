package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

public class YouPlayerResolver {

    private final PhysicalCard _gameTextCard;

    public YouPlayerResolver(PhysicalCard gameTextCard) {
        _gameTextCard = gameTextCard;
    }

    public String getPlayerName() {
        return _gameTextCard.getControllerName();
    }

}