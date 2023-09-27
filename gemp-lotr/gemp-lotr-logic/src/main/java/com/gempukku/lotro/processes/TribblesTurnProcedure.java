package com.gempukku.lotro.processes;

import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.game.PlayerOrderFeedback;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.gamestate.UserFeedback;

import java.util.Map;
import java.util.Set;

// Action generates multiple Effects, both costs and result of an action are Effects.

// Decision is also an Effect.
public class TribblesTurnProcedure extends TurnProcedure<TribblesGame> {
    Map<String, CardDeck> _decks;
    private final CardBlueprintLibrary _library;
    public TribblesTurnProcedure(TribblesGame tribblesGame, Map<String, CardDeck> decks, final UserFeedback userFeedback,
                                 CardBlueprintLibrary library, DefaultActionsEnvironment actionsEnvironment,
                                 final PlayerOrderFeedback playerOrderFeedback) {
        super(tribblesGame, tribblesGame.getPlayers(), userFeedback, actionsEnvironment, playerOrderFeedback);
        _decks = decks;
        _library = library;
    }

    @Override
    protected GameProcess setFirstGameProcess(TribblesGame game, Set<String> players, PlayerOrderFeedback playerOrderFeedback) {
        return new TribblesPlayerOrderProcess(_decks, _library, playerOrderFeedback);
    }

}
