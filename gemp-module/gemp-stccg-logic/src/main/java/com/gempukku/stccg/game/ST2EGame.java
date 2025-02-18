package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ST2EGameState;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.rules.generic.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private ST2EGameState _gameState;
    private TurnProcedure _turnProcedure;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, Map<String, PlayerClock> clocks,
                    final CardBlueprintLibrary library) {
        super(format, decks, library, GameType.SECOND_EDITION);

        _gameState = new ST2EGameState(decks.keySet(), this, clocks);
        RuleSet<ST2EGame> ruleSet = new RuleSet<>();
        ruleSet.applyRuleSet(this);

        _turnProcedure = new TurnProcedure(this);
        _gameState.setCurrentProcess(null);
    }


    @Override
    public ST2EGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

}