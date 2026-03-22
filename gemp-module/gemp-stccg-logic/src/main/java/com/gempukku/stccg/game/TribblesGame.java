package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerOrderProcess;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private TribblesGameState _gameState;
    private final TribblesRuleSet _rules;

    private TribblesGame(GameFormat format, Map<String, CardDeck> decks, CardBlueprintLibrary library,
                         GameResultListener listener) {
        super(format, decks, library, GameType.TRIBBLES, listener);
        _rules = new TribblesRuleSet();
        _rules.applyRuleSet(this);
    }

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, Map<String, PlayerClock> clocks,
                        final CardBlueprintLibrary library, GameResultListener listener) {
        this(format, decks, library, listener);
        try {
            _gameState = new TribblesGameState(decks.keySet(), clocks);
            _gameState.createPhysicalCards(this, library, decks);
            _gameState.setCurrentProcess(new TribblesPlayerOrderProcess(this));
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
            _cancelled = true;
        }
    }

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library,
                        GameTimer gameTimer) {
        this(format, decks, library, (GameResultListener) null);
        try {
            _gameState = new TribblesGameState(decks.keySet(), gameTimer);
            _gameState.createPhysicalCards(this, library, decks);
            _gameState.setCurrentProcess(new TribblesPlayerOrderProcess(this));
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
            _cancelled = true;
        }
    }


    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }

    @Override
    public TribblesRuleSet getRules() {
        return _rules;
    }

}