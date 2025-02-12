package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.hall.GameSettings;

public class ST2EGameMediator extends CardGameMediator {
    private final ST2EGame _game;

    public ST2EGameMediator(String gameId, GameParticipant[] participants, CardBlueprintLibrary library,
                            GameSettings gameSettings) {
        super(gameId, participants, gameSettings);
        _game = new ST2EGame(gameSettings.getGameFormat(), _playerDecks, _playerClocks, library);
    }


    @Override
    public final ST2EGame getGame() { return _game; }

}