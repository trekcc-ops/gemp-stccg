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
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.processes.st1e.ST1EPlayerOrderProcess;
import com.gempukku.stccg.rules.st1e.AffiliationAttackRestrictions;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;

import java.util.Collection;
import java.util.LinkedList;
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
            _gameState.createPhysicalCards(library, decks, this);
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
            _gameState.createPhysicalCards(library, decks, this);
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
    public boolean shouldAutoPass(Phase phase) {
            // If false for a given phase, the user will still be prompted to "Pass" even if they have no legal actions.
        Collection<Phase> autoPassPhases = new LinkedList<>();
        autoPassPhases.add(Phase.SEED_DOORWAY);
        autoPassPhases.add(Phase.SEED_MISSION);
        autoPassPhases.add(Phase.SEED_DILEMMA);
        autoPassPhases.add(Phase.SEED_FACILITY);
        autoPassPhases.add(Phase.CARD_PLAY);
        autoPassPhases.add(Phase.END_OF_TURN);
        return autoPassPhases.contains(phase);
    }

    public ST1ERuleSet getRules() { return _rules; }

    public PhysicalCard addCardToGame(String blueprintId, String playerId)
            throws CardNotFoundException {
        int cardId = _gameState.getAndIncrementNextCardId();
        PhysicalCard card = createPhysicalCard(blueprintId, cardId, playerId);
        _gameState.addCardToListOfAllCards(card);
        card.setZone(Zone.VOID);
        return card;
    }

}