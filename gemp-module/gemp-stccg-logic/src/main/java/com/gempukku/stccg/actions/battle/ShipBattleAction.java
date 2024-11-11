package com.gempukku.stccg.actions.battle;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShipBattleAction extends ActionyAction {
        // TODO - For now, ignores affiliation attack restrictions, as well as tactics, as well as leadership requirements
        // TODO - Very much not complete
                // i.e. it is just an action to compare numbers

    protected final PhysicalCard _actionSource;
    private boolean _openedFire;
    private boolean _actionWasInitiated = false;
    private boolean _returningFire;
    private boolean _virtualCardAction;
    protected Effect _finalEffect;
    private final ST1ELocation _location;
    private boolean _returnFireDecisionMade;
    private boolean _damageApplied;
    private final Player _attackingPlayer;
    private Player _defendingPlayer;
    private boolean _returnedFire;
    private final Map<Player, PhysicalCard> _targets = new HashMap<>();
    private final Map<Player, Collection<PhysicalCard>> _forces = new HashMap<>();
    private final Map<Player, OpenFireResult> _openFireResults = new HashMap<>();
    private final Map<Player, Integer> _damageSustained = new HashMap<>();
    private boolean _winnerDetermined;
    private boolean _battleResolved;

    private enum OpenFireResult {
        HIT, DIRECT_HIT, MISS
    }

    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public ShipBattleAction(PhysicalCard actionSource, Player performingPlayer, ST1ELocation location)
            throws InvalidGameLogicException {
        super(performingPlayer.getPlayerId(), ActionType.BATTLE);
        DefaultGame game = actionSource.getGame();
        setText("Initiate battle");
        _actionSource = actionSource;
        _location = location;
        _attackingPlayer = performingPlayer;
        _defendingPlayer = game.getPlayer(game.getOpponent(_performingPlayerId));
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !getEligibleCardsForForce(_attackingPlayer).isEmpty() && !getTargetOptions(_attackingPlayer).isEmpty();
    }

//    protected Effect getFinalEffect() { return new DefaultEffect(); }
    private Collection<PhysicalCard> getEligibleCardsForForce(Player player) {
            // TODO - Every ship needs a leader aboard
            // TODO - Each ship must have at least one matching, compatible personnel aboard
            // TODO - Attacking cards must be compatible
        // TODO - Ignores non-ship cards that can participate in battle
        return Filters.filterYourActive(
                player, Filters.ship, Filters.undocked, Filters.atLocation(_location));
    }

    private Collection<PhysicalCard> getTargetOptions(Player player) {
        return Filters.filterActive(player.getGame(), Filters.or(Filters.and(Filters.ship, Filters.undocked),
                        Filters.facility),
                Filters.atLocation(_location), Filters.not(Filters.your(player)));
    }

    private Effect selectForceEffect(Player player) {
        return new ChooseCardsOnTableEffect(this, player,
                "Choose ships to include in force", 1, getEligibleCardsForForce(player).size(),
                getEligibleCardsForForce(player)) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                _forces.put(player, selectedCards);
            }
        };
    }

    private Effect getTargetEffect(Player player) {
        return new ChooseCardsOnTableEffect(this, player,
                "Choose target", 1, 1, getTargetOptions(player)) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                _targets.put(player, Iterables.getOnlyElement(selectedCards));
            }
        };
    }

    private OpenFireResult calculateOpenFireResult(Player player) {
        String playerId = player.getPlayerId();
        int attackTotal = 0;
        for (PhysicalCard ship : _forces.get(player)) {
            attackTotal += ship.getBlueprint().getAttribute(CardAttribute.WEAPONS);
        }
        int defenseTotal = _targets.get(player).getBlueprint().getAttribute(CardAttribute.SHIELDS);
        player.getGame().sendMessage(playerId + " opens fire");
        player.getGame().sendMessage("ATTACK: " + attackTotal + ", DEFENSE: " + defenseTotal);
        if (attackTotal > defenseTotal * 2)
            return OpenFireResult.DIRECT_HIT;
        else if (attackTotal > defenseTotal)
            return OpenFireResult.HIT;
        else return OpenFireResult.MISS;
    }

    public Action nextAction(DefaultGame cardGame) {

        if (!_actionWasInitiated) {
            _actionWasInitiated = true;
        }

        if (_forces.get(_attackingPlayer) == null) {
            // TODO - Need to include some compatibility check here
            return new SubAction(this, selectForceEffect(_attackingPlayer));
        }

        if (_targets.get(_attackingPlayer) == null) {
            return new SubAction(this, getTargetEffect(_attackingPlayer));
        }

        if (!_returnFireDecisionMade) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new YesNoDecision(_defendingPlayer, "Do you want to return fire?") {
                @Override
                        protected void yes() {
                    _returnFireDecisionMade = true;
                    _returningFire = true;
                }
                @Override
                        protected void no() {
                    _returnFireDecisionMade = true;
                    _returningFire = false;
                }
                    }
            );
        }

        if (_returningFire && _forces.get(_defendingPlayer) == null) {
            return new SubAction(this, selectForceEffect(_defendingPlayer));
        }

        if (_returningFire && _targets.get(_defendingPlayer) == null) {
            return new SubAction(this, getTargetEffect(_defendingPlayer));
        }

        if (!_openedFire) {
            _openedFire = true;
            _openFireResults.put(_attackingPlayer, calculateOpenFireResult(_attackingPlayer));
        }

        if (!_returnedFire && _returningFire) {
            _returnedFire = true;
            _openFireResults.put(_defendingPlayer, calculateOpenFireResult(_defendingPlayer));
        }

        if (!_damageApplied) {
            _damageSustained.put(_attackingPlayer, getDamage(_openFireResults.get(_defendingPlayer)));
            _damageSustained.put(_defendingPlayer, getDamage(_openFireResults.get(_attackingPlayer)));
            _damageApplied = true;
        }

        if (!_winnerDetermined) {
            Player _winner;
            boolean _noWinner;
            if (_damageSustained.get(_attackingPlayer) > _damageSustained.get(_defendingPlayer))
                _winner = _defendingPlayer;
            else if (_damageSustained.get(_defendingPlayer) > _damageSustained.get(_attackingPlayer))
                _winner = _attackingPlayer;
            else _noWinner = true;
            _winnerDetermined = true;
        }

                // TODO - Commented out below because I need to define some additional methods for this to work
/*        if (!_battleResolved) {
            _targets.get(_attackingPlayer).applyDamage(_damageSustained.get(_defendingPlayer));
            _targets.get(_defendingPlayer).applyDamage(_damageSustained.get(_attackingPlayer));
            for (PhysicalCard card : _forces.get(_attackingPlayer)) {
                if (!card.isDestroyed())
                    card.stop();
            }
            for (PhysicalCard card : _forces.get(_defendingPlayer)) {
                if (!card.isDestroyed())
                    card.stop();
            }
            _battleResolved = true;
        }*/
        return getNextAction();
    }

    private Integer getDamage(OpenFireResult result) {
        if (result == OpenFireResult.DIRECT_HIT)
            return 100;
        else if (result == OpenFireResult.HIT)
            return 50;
        else return 0;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionSource;
    }

    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public boolean wasCarriedOut() {
        return _finalEffect != null && _finalEffect.wasCarriedOut();
    }

}