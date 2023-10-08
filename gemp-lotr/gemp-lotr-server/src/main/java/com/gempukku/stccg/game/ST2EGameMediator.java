package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.GameTimer;

public class ST2EGameMediator extends CardGameMediator<ST2EGame> {
    private final ST2EGame _game;
    public ST2EGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                            CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                            boolean showInGameHall) {
        super(gameId, participants, library, gameTimer, allowSpectators, showInGameHall);
        _game = new ST2EGame(gameFormat, _playerDecks, _userFeedback, library);
        _userFeedback.setGame(_game);
    }

    public ST2EGame getGame() { return _game; }

}
