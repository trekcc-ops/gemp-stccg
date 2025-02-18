package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerOrderProcess;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private TribblesGameState _gameState;
    private final TribblesRuleSet _rules;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, Map<String, PlayerClock> clocks,
                        final CardBlueprintLibrary library) {
        super(format, decks, library, GameType.TRIBBLES);
        _gameState = new TribblesGameState(decks.keySet(), this, clocks);
        _rules = new TribblesRuleSet();
        _rules.applyRuleSet(this);
        _gameState.createPhysicalCards(this, library, decks);
        _turnProcedure = new TurnProcedure(this);
        _gameState.setCurrentProcess(new TribblesPlayerOrderProcess(this));
    }

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library,
                        GameTimer gameTimer) {
        super(format, decks, library, GameType.TRIBBLES);
        _gameState = new TribblesGameState(decks.keySet(), this, gameTimer);
        _rules = new TribblesRuleSet();
        _rules.applyRuleSet(this);
        _gameState.createPhysicalCards(this, library, decks);
        _turnProcedure = new TurnProcedure(this);
        _gameState.setCurrentProcess(new TribblesPlayerOrderProcess(this));
    }


    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

}