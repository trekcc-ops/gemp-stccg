package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.hall.GameSettings;

public class TribblesGameMediator extends CardGameMediator {
    private final TribblesGame _tribblesgame;
    public TribblesGameMediator(String gameId, GameParticipant[] participants, CardBlueprintLibrary library,
                                GameSettings gameSettings) {
        super(gameId, participants, gameSettings);
        _tribblesgame = new TribblesGame(gameSettings.getGameFormat(), _playerDecks, library);
    }
    @Override
    public final TribblesGame getGame() { return _tribblesgame; }

}