package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.TribblesPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.tribbles.TribblesRuleSet;

import java.util.Map;
import java.util.Set;

public class TribblesGame extends DefaultGame {
    private final TribblesGameState _gameState;
    private final TurnProcedure _turnProcedure;
    private final TribblesGame _thisGame;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);
        _thisGame = this;

        _gameState = new TribblesGameState(_allPlayers, decks, library, _format, this);
        new TribblesRuleSet(_actionsEnvironment, _modifiersLogic, this).applyRuleSet();

        _gameState.createPhysicalCards();
        _turnProcedure = new TurnProcedure(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected GameProcess setFirstGameProcess(Set<String> players,
                                                      PlayerOrderFeedback playerOrderFeedback) {
                return new TribblesPlayerOrderProcess(decks, _library, playerOrderFeedback, _thisGame);
            }
        };
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

    public boolean isNextInSequence(PhysicalCard card) {
        final int cardValue = card.getBlueprint().getTribbleValue();
        if (_gameState.isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _gameState.getNextTribbleInSequence());
    }

}
