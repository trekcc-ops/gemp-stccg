package com.gempukku.stccg.actions.battle;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.targetresolver.BattleInitiationResolver;
import com.gempukku.stccg.cards.physicalcard.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class InitiateShipBattleAction extends ActionyAction implements TopLevelSelectableAction {
        // TODO - For now, ignores affiliation attack restrictions, as well as tactics, as well as leadership requirements
        // TODO - Very much not complete
                // i.e. it is just an action to compare numbers

    private boolean _openedFire;
    private String _winnerName;
    private boolean _noWinner;
    private boolean _returningFire;
    private boolean _damageApplied;
    private final String _defendingPlayerName;
    private boolean _returnedFire;
    private final Map<String, CardWithHullIntegrity> _targets = new HashMap<>();
    private final Map<String, Collection<CardWithHullIntegrity>> _forcesNew = new HashMap<>();
    private final Map<String, OpenFireResult> _openFireResults = new HashMap<>();
    private final Map<String, Integer> _damageSustained = new HashMap<>();
    private boolean _winnerDetermined;
    private boolean _battleResolved;
    private final BattleInitiationResolver _resolver;

    private enum OpenFireResult {
        HIT, DIRECT_HIT, MISS
    }

    public InitiateShipBattleAction(Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap, DefaultGame game,
                                    Player performingPlayer) {
        super(game, performingPlayer, ActionType.BATTLE);
        _defendingPlayerName = game.getOpponent(_performingPlayerId);
        _resolver = new BattleInitiationResolver(_performingPlayerId, _defendingPlayerName, targetMap);
        _cardTargets.add(_resolver);
    }

    private OpenFireResult calculateOpenFireResult(String playerName, DefaultGame cardGame) {
        float attackTotal = 0;
        for (CardWithHullIntegrity ship : _forcesNew.get(playerName)) {
            attackTotal += ship.getWeapons(cardGame);
        }
        float defenseTotal = _targets.get(playerName).getShields(cardGame);
        if (attackTotal > defenseTotal * 2)
            return OpenFireResult.DIRECT_HIT;
        else if (attackTotal > defenseTotal)
            return OpenFireResult.HIT;
        else return OpenFireResult.MISS;
    }

    protected void processEffect(DefaultGame cardGame) {
        _targets.putAll(_resolver.getTargets());
        _forcesNew.putAll(_resolver.getForces());
        _returningFire = _resolver.isReturningFire();
        String attackingPlayerName = _performingPlayerId;
        if (!_openedFire) {
            _openedFire = true;
            _openFireResults.put(attackingPlayerName, calculateOpenFireResult(attackingPlayerName, cardGame));
        }

        if (!_returnedFire && _returningFire) {
            _returnedFire = true;
            _openFireResults.put(_defendingPlayerName, calculateOpenFireResult(_defendingPlayerName, cardGame));
        }

        if (!_damageApplied) {
            _damageSustained.put(attackingPlayerName, getDamage(_openFireResults.get(_defendingPlayerName)));
            _damageSustained.put(_defendingPlayerName, getDamage(_openFireResults.get(attackingPlayerName)));
            _damageApplied = true;
        }

        if (!_winnerDetermined) {
            if (_damageSustained.get(attackingPlayerName) > _damageSustained.get(_defendingPlayerName))
                _winnerName = _defendingPlayerName;
            else if (_damageSustained.get(_defendingPlayerName) > _damageSustained.get(attackingPlayerName))
                _winnerName = attackingPlayerName;
            else _noWinner = true;
            _winnerDetermined = true;
        }

        if (!_battleResolved) {
            if (_targets.get(attackingPlayerName) != null) {
                _targets.get(attackingPlayerName).applyDamage(_damageSustained.get(_defendingPlayerName));
            }

            if (_targets.get(_defendingPlayerName) != null) {
                _targets.get(_defendingPlayerName).applyDamage(_damageSustained.get(attackingPlayerName));
            }

            for (CardWithHullIntegrity card : _forcesNew.get(attackingPlayerName)) {
                if (card.getHullIntegrity() > 0)
                    card.stop();
            }
            for (CardWithHullIntegrity card : _forcesNew.get(_defendingPlayerName)) {
                if (card.getHullIntegrity() > 0)
                    card.stop();
            }
            _battleResolved = true;
        }
        setAsSuccessful();
    }

    private Integer getDamage(OpenFireResult result) {
        if (result == null) {
            return 0;
        } else return switch(result) {
            case DIRECT_HIT -> 100;
            case HIT -> 50;
            case MISS -> 0;
        };
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return null;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    public boolean wasWonBy(Player player) {
        return Objects.equals(_winnerName, player.getPlayerId());
    }

}