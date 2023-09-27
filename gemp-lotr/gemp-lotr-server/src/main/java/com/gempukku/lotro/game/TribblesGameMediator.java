package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.hall.GameTimer;

public class TribblesGameMediator extends CardGameMediator<TribblesGame> {
    private final TribblesGame _tribblesgame;
    public TribblesGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                                CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                                boolean showInGameHall) {
        super(gameId, participants, library, gameTimer, allowSpectators, showInGameHall);
        _tribblesgame = new TribblesGame(gameFormat, _playerDecks, _userFeedback, library);
        _userFeedback.setGame(_tribblesgame);
    }

    public TribblesGame getGame() { return _tribblesgame; }

}
