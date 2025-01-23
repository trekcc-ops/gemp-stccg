package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerOrderProcess;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private TribblesGameState _gameState;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new TribblesGameState(decks.keySet(), this);
        new TribblesRuleSet(this).applyRuleSet(this);

        _gameState.createPhysicalCards(library, decks);
        _turnProcedure = new TurnProcedure(this);
        setCurrentProcess(new TribblesPlayerOrderProcess(this));
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

}