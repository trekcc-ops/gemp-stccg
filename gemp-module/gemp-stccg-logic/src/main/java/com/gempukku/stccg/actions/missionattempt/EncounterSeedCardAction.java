package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class EncounterSeedCardAction extends AbstractCostToEffectAction {
    private final AttemptingUnit _attemptingUnit;
    private final ST1EGame _game;
    private boolean _seedCardWasRevealed;
    private final List<String> _seedCards;

    public EncounterSeedCardAction(AttemptMissionAction action, List<String> seedCards) {
        super(action.getPlayer(), ActionType.OTHER);
        _game = action.getGame();
        _attemptingUnit = action.getAttemptingEntity();
        _seedCards = seedCards;
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return null;
    }

    @Override
    public Effect nextEffect() throws InvalidGameLogicException {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_seedCardWasRevealed) {
            String cardEncountered = _seedCards.getFirst();
            _game.sendMessage("Seed card encountered: " + cardEncountered);
            _seedCardWasRevealed = true;
            _seedCards.removeFirst();
        }

        return getNextEffect();
    }

    @Override
    public ST1EGame getGame() { return _game; }

    public AttemptingUnit getAttemptingUnit() { return _attemptingUnit; }

}
