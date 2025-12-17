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
    private final RuleSet<ST2EGame> _rules;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, Map<String, PlayerClock> clocks,
                    final CardBlueprintLibrary library, GameResultListener listener) {
        super(format, decks, library, GameType.SECOND_EDITION, listener);
        _rules = new RuleSet<>();
        _rules.applyRuleSet(this);

        try {
            _gameState = new ST2EGameState(decks.keySet(), clocks);
            _gameState.setCurrentProcess(null);
        } catch(InvalidGameOperationException exp) {
            sendErrorMessage(exp);
            _cancelled = true;
        }
    }


    @Override
    public ST2EGameState getGameState() {
        return _gameState;
    }

    @Override
    public RuleSet getRules() {
        return _rules;
    }

}