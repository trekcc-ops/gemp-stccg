package com.gempukku.stccg.game;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.hall.GameCreationListener;

import java.util.Map;

public class GameRecordingCreationListener implements GameCreationListener {

    private final GameHistoryService _gameHistoryService;

    public GameRecordingCreationListener(GameHistoryService gameHistoryService) {
        _gameHistoryService = gameHistoryService;
    }
    @Override
    public void process(CardGameMediator mediator) {
        Map<String, CardDeck> decks = mediator.getDecks();
        final var gameRecordingInProgress = _gameHistoryService.recordGame(mediator, mediator.getName(), decks);
        mediator.addResultListener(new GameRecordingResultListener(mediator.getPlayers(), gameRecordingInProgress));
    }
}