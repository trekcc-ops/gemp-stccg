package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.processes.st1e.ST1EPlayerOrderProcess;
import com.gempukku.stccg.rules.st1e.AffiliationAttackRestrictions;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;

import java.util.Map;

public class ST1EGame extends DefaultGame {
    private ST1EGameState _gameState;
    private final ST1ERuleSet _rules;

    private ST1EGame(GameFormat format, Map<String, CardDeck> decks, CardBlueprintLibrary library,
                     GameResultListener listener) {
        super(format, decks, library, GameType.FIRST_EDITION, listener);
        _rules = new ST1ERuleSet();
    }


    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, Map<String, PlayerClock> clocks,
                    final CardBlueprintLibrary library, GameResultListener resultListener) {
        this(format, decks, library, resultListener);
        try {
            _gameState = new ST1EGameState(decks.keySet(), clocks);
            _gameState.createPhysicalCards(decks, this);
            _gameState.setCurrentProcess(new ST1EPlayerOrderProcess());
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
            _cancelled = true;
        }
        _rules.applyRuleSet(this);
    }

    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library,
                    GameTimer gameTimer) throws InvalidGameOperationException {
        this(format, decks, library, (GameResultListener) null);

        try {
            _gameState = new ST1EGameState(decks.keySet(), gameTimer);
            _gameState.createPhysicalCards(decks, this);
            _gameState.setCurrentProcess(new ST1EPlayerOrderProcess());
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
            _cancelled = true;
        }
        _rules.applyRuleSet(this);
    }



    @Override
    public ST1EGameState getGameState() {
        return _gameState;
    }

    public void setAffiliationAttackRestrictions(AffiliationAttackRestrictions restrictions) {
    }

    @Override
    public boolean shouldAutoPass(Phase phase, String playerName) {
        return switch(phase) {
            case SEED_DOORWAY, SEED_MISSION, SEED_DILEMMA, SEED_FACILITY -> true;
            case CARD_PLAY, END_OF_TURN, START_OF_TURN -> true;
            case EXECUTE_ORDERS -> !playerName.equals(getCurrentPlayerId());
            case BETWEEN_TURNS, TRIBBLES_TURN -> false;
        };
    }

    public ST1ERuleSet getRules() { return _rules; }

    public PhysicalCard addCardToGame(String blueprintId, String playerId)
            throws CardNotFoundException {
        int cardId = _gameState.getAndIncrementNextCardId();
        PhysicalCard card = createPhysicalCard(blueprintId, cardId, playerId);
        _gameState.addCardToListOfAllCards(card);
        card.setZone(Zone.VOID);
        for (Modifier modifier : card.getAlwaysOnModifiers(this)) {
            _gameState.getModifiersLogic().addAlwaysOnModifier(modifier);
        }
        return card;
    }

}