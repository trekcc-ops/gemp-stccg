package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.RuleSet;
import com.gempukku.stccg.rules.WinConditionRule;
import com.gempukku.stccg.rules.lotronly.CharacterDeathRule;

import java.util.Map;
import java.util.Set;

public class LotroGame extends DefaultGame {

    private final GameState _gameState;
    private final TurnProcedure<DefaultGame> _turnProcedure;

    public LotroGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                     final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new RuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();
        new WinConditionRule(_actionsEnvironment).applyRule();
        new CharacterDeathRule(_actionsEnvironment).applyRule();

            // Being deprecated
        _gameState = new TribblesGameState(_allPlayers, decks, library, _format);
        _turnProcedure = new TurnProcedure<>(this, decks.keySet(), userFeedback, _actionsEnvironment,
                _gameState::init);
    }

    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        final Set<Phase> passablePhases = _autoPassConfiguration.get(playerId);
        if (passablePhases == null)
            return false;
        return passablePhases.contains(phase);
    }

    public GameState getGameState() {
        return _gameState;
    }
    public TurnProcedure<DefaultGame> getTurnProcedure() { return _turnProcedure; }

    // Dummy function. LotroGame will eventually be deprecated.
    public boolean checkPlayRequirements(PhysicalCard card) { return true; }
}
