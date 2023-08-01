package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.results.AssignAgainstResult;
import com.gempukku.lotro.effects.results.AssignedToSkirmishResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AssignmentPhaseEffect extends AbstractEffect {
    private final Map<LotroPhysicalCard, Set<LotroPhysicalCard>> _assignments;
    private final String _text;
    private final String _playerId;

    public AssignmentPhaseEffect(String playerId, Map<LotroPhysicalCard, Set<LotroPhysicalCard>> assignments, String text) {
        _playerId = playerId;
        // Sanitize the assignments
        _assignments = new HashMap<>();
        for (Map.Entry<LotroPhysicalCard, Set<LotroPhysicalCard>> physicalCardListEntry : assignments.entrySet()) {
            LotroPhysicalCard fpChar = physicalCardListEntry.getKey();
            Set<LotroPhysicalCard> minions = physicalCardListEntry.getValue();
            if (minions != null && minions.size() > 0)
                _assignments.put(fpChar, minions);
        }
        _text = text;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(DefaultGame game) {
        return _text;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_assignments.size() > 0) {
            game.getGameState().sendMessage(_playerId + " assigns characters to skirmish");
        }
        else {
            game.getGameState().sendMessage(_playerId + " skips assigning any characters");
        }
        for (Map.Entry<LotroPhysicalCard, Set<LotroPhysicalCard>> physicalCardListEntry : _assignments.entrySet()) {
            LotroPhysicalCard fpChar = physicalCardListEntry.getKey();
            Set<LotroPhysicalCard> minions = physicalCardListEntry.getValue();

            if (Filters.notAssignedToSkirmish.accepts(game, fpChar))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(fpChar, _playerId));
            for (LotroPhysicalCard notAssignedMinion : Filters.filter(minions, game, Filters.notAssignedToSkirmish))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(notAssignedMinion, _playerId));

            game.getGameState().assignToSkirmishes(fpChar, minions);


            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, fpChar, minions));
            for (LotroPhysicalCard minion : minions)
                game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, minion, fpChar));
        }
        return new FullEffectResult(true);
    }
}