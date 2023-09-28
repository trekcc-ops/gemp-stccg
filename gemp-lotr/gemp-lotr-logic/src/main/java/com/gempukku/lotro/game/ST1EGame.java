package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.gamestate.GameStateListener;
import com.gempukku.lotro.gamestate.UserFeedback;
import com.gempukku.lotro.processes.GameProcess;
import com.gempukku.lotro.processes.ST1EPlayerOrderProcess;
import com.gempukku.lotro.processes.TurnProcedure;
import com.gempukku.lotro.rules.RuleSet;

import java.util.Map;
import java.util.Set;

public class ST1EGame extends DefaultGame {
    private final GameState _gameState;
    private final TurnProcedure<ST1EGame> _turnProcedure;

    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new RuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState = new GameState(_cards, library, _format);
        _turnProcedure = new TurnProcedure<>(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected GameProcess setFirstGameProcess(ST1EGame game, Set<String> players,
                                                      PlayerOrderFeedback playerOrderFeedback) {
                return new ST1EPlayerOrderProcess(players, playerOrderFeedback);
            }
        };
    }


    @Override
    public GameState getGameState() {
        return _gameState;
    }
    public TurnProcedure<ST1EGame> getTurnProcedure() { return _turnProcedure; }
    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }
    

    public boolean checkPlayRequirements(LotroPhysicalCard card) {
//        _gameState.sendMessage("Calling game.checkPlayRequirements for card " + card.getBlueprint().getTitle());

        // Check if card's own play requirements are met
        if (card.getBlueprint().playRequirementsNotMet(this, card))
            return false;
        // Check if the card's playability has been modified in the current game state
        return !_modifiersLogic.canNotPlayCard(this, card.getOwner(), card);

    }

}
