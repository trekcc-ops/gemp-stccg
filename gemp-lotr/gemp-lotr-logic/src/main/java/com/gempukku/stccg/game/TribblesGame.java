package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.PhysicalCard;
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
    private final TurnProcedure<TribblesGame> _turnProcedure;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new TribblesRuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState = new TribblesGameState(_allPlayers, decks, library, _format);
        _gameState.createPhysicalCards();
        _turnProcedure = new TurnProcedure<>(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected GameProcess setFirstGameProcess(TribblesGame game, Set<String> players,
                                                      PlayerOrderFeedback playerOrderFeedback) {
                return new TribblesPlayerOrderProcess(decks, _library, playerOrderFeedback);
            }
        };
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure<TribblesGame> getTurnProcedure() { return _turnProcedure; }
    public boolean checkPlayRequirements(PhysicalCard card) {
//        _gameState.sendMessage("Calling game.checkPlayRequirements for card " + card.getBlueprint().getTitle());

        // Check if card's own play requirements are met
        if (card.getBlueprint().playRequirementsNotMet(this, card))
            return false;
        // Check if the card's playability has been modified in the current game state
        if (_modifiersLogic.canNotPlayCard(this, card.getOwner(), card))
            return false;

        // Otherwise, the play requirements are met if the card is next in the tribble sequence,
        // or if it can be played out of sequence
        return (isNextInSequence(card) || card.getBlueprint().canPlayOutOfSequence(this, card));
    }

    public boolean isNextInSequence(PhysicalCard card) {
        final int cardValue = card.getBlueprint().getTribbleValue();
        if (_gameState.isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _gameState.getNextTribbleInSequence());
    }

}
