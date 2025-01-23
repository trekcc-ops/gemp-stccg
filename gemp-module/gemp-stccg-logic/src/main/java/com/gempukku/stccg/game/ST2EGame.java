package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ST2EGameState;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private ST2EGameState _gameState;
    private TurnProcedure _turnProcedure;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new ST2EGameState(decks.keySet(), this);
        RuleSet<ST2EGame> ruleSet = new RuleSet<>();
        ruleSet.applyRuleSet(this);

        _turnProcedure = new TurnProcedure(this);
        setCurrentProcess(null);
    }


    @Override
    public ST2EGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

}