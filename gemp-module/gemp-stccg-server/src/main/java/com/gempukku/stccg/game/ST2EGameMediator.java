package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.GameTimer;

public class ST2EGameMediator extends CardGameMediator {
    private final ST2EGame _game;
    public ST2EGameMediator(String gameId, GameFormat gameFormat, GameParticipant[] participants,
                            CardBlueprintLibrary library, GameTimer gameTimer, boolean allowSpectators,
                            boolean showInGameHall) {
        super(gameId, participants, gameTimer, allowSpectators, showInGameHall);
        _game = new ST2EGame(gameFormat, _playerDecks, library);
    }

    @Override
    public final ST2EGame getGame() { return _game; }

}