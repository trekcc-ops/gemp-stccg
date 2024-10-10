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
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _context.setValueToMemory(_memoryId, result);
                            playerChosen();
                        }
                    });
    }

    protected void playerChosen() { }
}