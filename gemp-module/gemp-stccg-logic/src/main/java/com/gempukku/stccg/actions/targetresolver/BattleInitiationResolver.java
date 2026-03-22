package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.cards.physicalcard.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.ShipBattleTargetDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleInitiationResolver implements ActionTargetResolver {

    private final String _attackingPlayerName;
    ShipBattleTargetDecision _attackTargetDecision;
    private final Map<PhysicalCard, Map<String, List<PhysicalCard>>> _targetMap;
    private final String _defendingPlayerName;
    private final Map<String, List<CardWithHullIntegrity>> _forces = new HashMap<>();
    private final Map<String, CardWithHullIntegrity> _targets = new HashMap<>();
    private Boolean _returningFire;

    public BattleInitiationResolver(String playerName, String defendingPlayerName,
                                    Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap) {
        _attackingPlayerName = playerName;
        _defendingPlayerName = defendingPlayerName;
        _targetMap = targetMap;
    }
    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_attackTargetDecision == null) {
            _attackTargetDecision =
                    new ShipBattleTargetDecision(_attackingPlayerName, DecisionContext.SHIP_BATTLE_TARGETS,
                            _targetMap, cardGame);
            cardGame.sendAwaitingDecision(_attackTargetDecision);
        } else if (_forces.isEmpty() && _targets.isEmpty()) {
            // TODO - Not allowing player 2 to pick additional forces or targets yet
            _forces.put(_attackingPlayerName, _attackTargetDecision.getAttackingCards());
            _forces.put(_defendingPlayerName, List.of(_attackTargetDecision.getTarget()));
            _targets.put(_attackingPlayerName, _attackTargetDecision.getTarget());
            _returningFire = false; // TODO not returning fire
        }
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return false;
    }

    @Override
    public boolean isResolved() {
        return !_forces.isEmpty() && !_targets.isEmpty();
    }

    public Map<String, List<CardWithHullIntegrity>> getForces() {
        return _forces;
    }

    public Map<String, CardWithHullIntegrity> getTargets() {
        return _targets;
    }

    public boolean isReturningFire() {
        return _returningFire;
    }
}