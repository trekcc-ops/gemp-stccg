package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.processes.st1e.ST1EPlayerOrderProcess;
import com.gempukku.stccg.rules.st1e.AffiliationAttackRestrictions;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class ST1EGame extends DefaultGame {
    private ST1EGameState _gameState;
    private final ST1ERuleSet _rules;

    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library) {
        super(format, decks, library);

        _gameState = new ST1EGameState(decks.keySet(), this);
        _rules = new ST1ERuleSet(this);
        _rules.applyRuleSet();

        _gameState.createPhysicalCards(library, decks);
        _turnProcedure =
                new TurnProcedure(this, new ST1EPlayerOrderProcess(this));
    }

    @Override
    public ST1EGameState getGameState() {
        return _gameState;
    }

    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

    protected void restoreSnapshot() {
        if (_snapshotToRestore != null) {
            if (_snapshotToRestore.getGameState() instanceof ST1EGameState st1estate) {
                _gameState = st1estate;
                _gameState.setModifiersLogic(_snapshotToRestore.getModifiersLogic());
                _gameState.setActionsEnvironment(_snapshotToRestore.getActionsEnvironment());
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                sendStateToAllListeners();
            } else throw new RuntimeException("Tried to restore a snapshot with an invalid game state");
        }
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

}