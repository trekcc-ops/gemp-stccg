package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.AttemptingEntity;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class EncounterSeedCardAction extends AbstractCostToEffectAction {
    private AttemptingEntity _attemptingEntity;
    private final Player _player;
    private final ST1EGame _game;
    private String _cardEncountered;
    private boolean _seedCardWasRevealed;
    private final List<String> _seedCards;

    public EncounterSeedCardAction(AttemptMissionAction action, List<String> seedCards) {
        super(action.getPlayer(), ActionType.OTHER);
        _player = action.getPlayer();
        _game = action.getGame();
        _attemptingEntity = action.getAttemptingEntity();
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
    public Effect nextEffect() {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_seedCardWasRevealed) {
            _cardEncountered = _seedCards.get(0);
            _game.getGameState().sendMessage("Seed card encountered: " + _cardEncountered);
            _seedCardWasRevealed = true;
            _seedCards.remove(0);
        }

        return getNextEffect();
    }

    @Override
    public ST1EGame getGame() { return _game; }

}
