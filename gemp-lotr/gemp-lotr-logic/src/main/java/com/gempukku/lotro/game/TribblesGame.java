package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.gamestate.TribblesGameState;
import com.gempukku.lotro.gamestate.UserFeedback;
import com.gempukku.lotro.processes.TribblesTurnProcedure;
import com.gempukku.lotro.rules.tribbles.TribblesRuleSet;

import java.util.Map;

public class TribblesGame extends DefaultGame {
    private final TribblesGameState _gameState;
    private final TribblesTurnProcedure _turnProcedure;

    public TribblesGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new TribblesRuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState = new TribblesGameState();
        _turnProcedure = new TribblesTurnProcedure(this, decks, userFeedback, _library, _actionsEnvironment,
                (playerOrder, firstPlayer) -> _gameState.init(playerOrder, firstPlayer, _cards, _library, _format));
    }

    public TribblesGameState getGameState() {
        return _gameState;
    }
    public TribblesTurnProcedure getTurnProcedure() { return _turnProcedure; }
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
