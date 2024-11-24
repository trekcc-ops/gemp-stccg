package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.hall.GameSettings;

public class ST1EGameMediator extends CardGameMediator {
    private final ST1EGame _st1egame;
    public ST1EGameMediator(String gameId, GameParticipant[] participants, CardBlueprintLibrary library,
                            GameSettings gameSettings) {
        super(gameId, participants, gameSettings);
        _st1egame = new ST1EGame(gameSettings.getGameFormat(), _playerDecks, library);
    }

    @Override
    public final ST1EGame getGame() { return _st1egame; }

}