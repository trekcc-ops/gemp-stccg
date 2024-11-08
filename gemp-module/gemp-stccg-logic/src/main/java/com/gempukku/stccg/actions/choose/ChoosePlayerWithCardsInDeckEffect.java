package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlayerWithCardsInDeckEffect extends UnrespondableEffect {
    private final String _playerId;
    private final ActionContext _context;
    private final String _memoryId;

    public ChoosePlayerWithCardsInDeckEffect(ActionContext actionContext, String memoryId) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
        _context = actionContext;
        _memoryId = memoryId;
    }

    @Override
    public void doPlayEffect() {
        List<String> playersWithCards = new ArrayList<>();
        for (String player : _game.getAllPlayerIds()) {
            if (!_game.getGameState().getDrawDeck(player).isEmpty())
                playersWithCards.add(player);
        }
        if (playersWithCards.size() == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_playerId), "Choose a player",
                            playersWithCards) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _context.setValueToMemory(_memoryId, result);
                            playerChosen();
                        }
                    });
    }

    protected void playerChosen() { }
}