package com.gempukku.stccg.actions.battle;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.ShipBattleTargetDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitiateShipBattleAction extends ActionyAction implements TopLevelSelectableAction {
        // TODO - For now, ignores affiliation attack restrictions, as well as tactics, as well as leadership requirements
        // TODO - Very much not complete
                // i.e. it is just an action to compare numbers

    private boolean _openedFire;
    private boolean _actionWasInitiated = false;
    private Player _winner;
    private boolean _noWinner;
    private boolean _returningFire;
    private boolean _returnFireDecisionMade;
    private boolean _damageApplied;
    private Player _defendingPlayer;
    private boolean _returnedFire;
    private final Map<Player, CardWithHullIntegrity> _targets = new HashMap<>();
    private final Map<Player, Collection<CardWithHullIntegrity>> _forces = new HashMap<>();
    private final Map<Player, OpenFireResult> _openFireResults = new HashMap<>();
    private final Map<Player, Integer> _damageSustained = new HashMap<>();
    private boolean _winnerDetermined;
    private boolean _battleResolved;
    private final Map<PhysicalCard, Map<String, List<PhysicalCard>>> _targetMap;
    private ShipBattleTargetDecision _attackTargetDecision;

    private enum OpenFireResult {
        HIT, DIRECT_HIT, MISS
    }

    public InitiateShipBattleAction(Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap, DefaultGame game, Player performingPlayer) {
        super(game, performingPlayer, ActionType.BATTLE);
        _targetMap = targetMap;
    }

    private OpenFireResult calculateOpenFireResult(Player player) {
        int attackTotal = 0;
        for (PhysicalCard ship : _forces.get(player)) {
            attackTotal += ship.getBlueprint().getWeapons();
        }
        int defenseTotal = _targets.get(player).getBlueprint().getShields();
        if (attackTotal > defenseTotal * 2)
            return OpenFireResult.DIRECT_HIT;
        else if (attackTotal > defenseTotal)
            return OpenFireResult.HIT;
        else return OpenFireResult.MISS;
    }

    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException, InvalidGameLogicException {

        Player attackingPlayer = cardGame.getPlayer(_performingPlayerId);
        _defendingPlayer = cardGame.getOpponent(attackingPlayer);
        if (!_actionWasInitiated) {

            if (_attackTargetDecision == null) {
                _attackTargetDecision = new ShipBattleTargetDecision(attackingPlayer, DecisionContext.SHIP_BATTLE_TARGETS, _targetMap, cardGame);
                cardGame.sendAwaitingDecision(_attackTargetDecision);
            } else {
                // TODO - Not allowing player 2 to pick additional forces or targets yet
                _forces.put(attackingPlayer, _attackTargetDecision.getAttackingCards());
                _forces.put(_defendingPlayer, List.of(_attackTargetDecision.getTarget()));
                _targets.put(attackingPlayer, _attackTargetDecision.getTarget());
                _actionWasInitiated = true;
                _returningFire = false; // TODO not returning fire
            }
        }

        if (_actionWasInitiated) {
            if (!_openedFire) {
                _openedFire = true;
                _openFireResults.put(attackingPlayer, calculateOpenFireResult(attackingPlayer));
            }

            if (!_returnedFire && _returningFire) {
                _returnedFire = true;
                _openFireResults.put(_defendingPlayer, calculateOpenFireResult(_defendingPlayer));
            }

            if (!_damageApplied) {
                _damageSustained.put(attackingPlayer, getDamage(_openFireResults.get(_defendingPlayer)));
                _damageSustained.put(_defendingPlayer, getDamage(_openFireResults.get(attackingPlayer)));
                _damageApplied = true;
            }

            if (!_winnerDetermined) {
                if (_damageSustained.get(attackingPlayer) > _damageSustained.get(_defendingPlayer))
                    _winner = _defendingPlayer;
                else if (_damageSustained.get(_defendingPlayer) > _damageSustained.get(attackingPlayer))
                    _winner = attackingPlayer;
                else _noWinner = true;
                _winnerDetermined = true;
            }

            if (!_battleResolved) {
                if (_targets.get(attackingPlayer) != null) {
                    _targets.get(attackingPlayer).applyDamage(_damageSustained.get(_defendingPlayer));
                }

                if (_targets.get(_defendingPlayer) != null) {
                    _targets.get(_defendingPlayer).applyDamage(_damageSustained.get(attackingPlayer));
                }

                for (CardWithHullIntegrity card : _forces.get(attackingPlayer)) {
                    if (card.getHullIntegrity() > 0)
                        card.stop();
                }
                for (CardWithHullIntegrity card : _forces.get(_defendingPlayer)) {
                    if (card.getHullIntegrity() > 0)
                        card.stop();
                }
                _battleResolved = true;
            }

            setAsSuccessful();
        }


        return getNextAction();
    }

    private Integer getDamage(OpenFireResult result) {
        if (result == OpenFireResult.DIRECT_HIT)
            return 100;
        else if (result == OpenFireResult.HIT)
            return 50;
        else if (result == OpenFireResult.MISS)
            return 0;
        else return 0;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return null;
    }

    public boolean wasCarriedOut() {
        return _wasCarriedOut = true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    public boolean wasWonBy(Player player) {
        return _winner == player;
    }

}