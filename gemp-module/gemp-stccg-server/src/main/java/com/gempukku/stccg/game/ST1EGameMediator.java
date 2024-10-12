package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.GameTimer;

public class ST1EGameMediator extends CardGameMediator {
    private final ST1EGame _st1egame;
    public ST1EGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                            CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                            boolean showInGameHall) {
        super(gameId, participants, gameTimer, allowSpectators, showInGameHall);
        _st1egame = new ST1EGame(gameFormat, _playerDecks, library);
    }

    @Override
    public ST1EGame getGame() { return _st1egame; }

}