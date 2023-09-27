package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.hall.GameTimer;

public class ST1EGameMediator extends CardGameMediator<ST1EGame> {
    private final ST1EGame _st1egame;
    public ST1EGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                            CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                            boolean showInGameHall) {
        super(gameId, participants, library, gameTimer, allowSpectators, showInGameHall);
        _st1egame = new ST1EGame(gameFormat, _playerDecks, _userFeedback, library);
        _userFeedback.setGame(_st1egame);
    }

    public ST1EGame getGame() { return _st1egame; }

}
