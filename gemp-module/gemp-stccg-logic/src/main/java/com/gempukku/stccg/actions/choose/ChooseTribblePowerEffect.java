package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

public class ChooseTribblePowerEffect extends UnrespondableEffect {
    private final String _playerId;
    private final ActionContext _context;
    private final String _memoryId;
    public ChooseTribblePowerEffect(ActionContext actionContext, String memoryId) {
        super(actionContext);
        _context = actionContext;
        _memoryId = memoryId;
        _playerId = actionContext.getPerformingPlayerId();
    }

    @Override
    public void doPlayEffect() {
        String[] powers = TribblePower.names();
        _game.getUserFeedback().sendAwaitingDecision(_playerId,
                new MultipleChoiceAwaitingDecision("Choose a Tribble power", powers) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        _context.setValueToMemory(_memoryId, result);
                        powerChosen();
                    }
                });
    }

    protected void powerChosen() { }
}