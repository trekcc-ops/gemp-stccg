package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.gamestate.TribblesGameState;
import com.gempukku.lotro.gamestate.UserFeedback;
import com.gempukku.lotro.processes.GameProcess;
import com.gempukku.lotro.processes.TribblesPlayerOrderProcess;
import com.gempukku.lotro.processes.TurnProcedure;
import com.gempukku.lotro.rules.tribbles.TribblesRuleSet;

import java.util.Map;
import java.util.Set;

public class TribblesGame extends DefaultGame {
    private final TribblesGameState _gameState;
    private final TurnProcedure<TribblesGame> _turnProcedure;
//    private final TribblesTurnProcedure _turnProcedure;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new TribblesRuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState = new TribblesGameState(_cards, library, _format);
        _turnProcedure = new TurnProcedure<>(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected GameProcess setFirstGameProcess(TribblesGame game, Set<String> players,
                                                      PlayerOrderFeedback playerOrderFeedback) {
                return new TribblesPlayerOrderProcess(decks, _library, playerOrderFeedback);
            }
        };
/*        _turnProcedure = new TribblesTurnProcedure(this, decks, userFeedback, _library, _actionsEnvironment,
                (playerOrder, firstPlayer) -> _gameState.init(playerOrder, firstPlayer, _cards, _library, _format));*/
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure<TribblesGame> getTurnProcedure() { return _turnProcedure; }
    public boolean checkPlayRequirements(LotroPhysicalCard card) {
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

    public boolean isNextInSequence(LotroPhysicalCard card) {
        final int cardValue = card.getBlueprint().getTribbleValue();
        if (_gameState.isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _gameState.getNextTribbleInSequence());
    }

}
