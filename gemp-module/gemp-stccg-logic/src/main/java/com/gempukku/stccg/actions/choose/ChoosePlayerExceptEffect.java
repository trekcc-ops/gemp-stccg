package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.Objects;

public class ChoosePlayerExceptEffect extends UnrespondableEffect {
    private final String _playerId;
    private final String _excludedPlayerId;
    private final ActionContext _context;
    private final String _memoryId;

    public ChoosePlayerExceptEffect(ActionContext actionContext, String excludedPlayerId, String memoryId) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
        _excludedPlayerId = excludedPlayerId;
        _context = actionContext;
        _memoryId = memoryId;
    }

    @Override
    public void doPlayEffect() {
        String[] allPlayers = _game.getAllPlayerIds();
        String[] includedPlayers = new String[allPlayers.length - 1];
        int j = 0;
        for (String allPlayer : allPlayers) {
            if (!Objects.equals(allPlayer, _excludedPlayerId)) {
                includedPlayers[j] = allPlayer;
                j++;
            }
        }
        if (includedPlayers.length == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", includedPlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _context.setValueToMemory(_memoryId, result);
                            playerChosen();
                        }
                    });
    }

    protected void playerChosen() { }
}