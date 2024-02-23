package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.GameTimer;

public class TribblesGameMediator extends CardGameMediator {
    private final TribblesGame _tribblesgame;
    public TribblesGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                                CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                                boolean showInGameHall) {
        super(gameId, participants, library, gameTimer, allowSpectators, showInGameHall);
        _tribblesgame = new TribblesGame(gameFormat, _playerDecks, _userFeedback, library);
        _userFeedback.setGame(_tribblesgame);
    }
    @Override
    public TribblesGame getGame() { return _tribblesgame; }

}
