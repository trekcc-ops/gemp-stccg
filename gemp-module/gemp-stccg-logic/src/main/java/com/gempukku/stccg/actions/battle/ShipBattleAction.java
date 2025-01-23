package com.gempukku.stccg.actions.battle;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShipBattleAction extends ActionyAction implements TopLevelSelectableAction {
        // TODO - For now, ignores affiliation attack restrictions, as well as tactics, as well as leadership requirements
        // TODO - Very much not complete
                // i.e. it is just an action to compare numbers

    protected final PhysicalCard _actionSource;
    private boolean _openedFire;
    private boolean _actionWasInitiated = false;
    private boolean _returningFire;
    private boolean _virtualCardAction;
    private final MissionLocation _location;
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
    public ShipBattleAction(DefaultGame cardGame, PhysicalCard actionSource, Player performingPlayer,
                            MissionLocation location)
            throws InvalidGameLogicException, PlayerNotFoundException {
        super(cardGame, performingPlayer, "Initiate battle", ActionType.BATTLE);
        DefaultGame game = actionSource.getGame();
        _actionSource = actionSource;
        _location = location;
        _attackingPlayer = performingPlayer;
        _defendingPlayer = game.getPlayer(game.getOpponent(_performingPlayerId));
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !getEligibleCardsForForce(_attackingPlayer, cardGame).isEmpty() &&
                !getTargetOptions(_attackingPlayer, cardGame).isEmpty();
    }

//    protected Effect getFinalEffect() { return new DefaultEffect(); }
    private Collection<PhysicalCard> getEligibleCardsForForce(Player player, DefaultGame cardGame) {
            // TODO - Every ship needs a leader aboard
            // TODO - Each ship must have at least one matching, compatible personnel aboard
            // TODO - Attacking cards must be compatible
        // TODO - Ignores non-ship cards that can participate in battle
        return Filters.filterYourActive(cardGame,
                player, Filters.ship, Filters.undocked, Filters.atLocation(_location));
    }

    private Collection<PhysicalCard> getTargetOptions(Player player, DefaultGame cardGame) {
        return Filters.filterActive(cardGame, Filters.or(Filters.and(Filters.ship, Filters.undocked),
                        Filters.facility),
                Filters.atLocation(_location), Filters.not(Filters.your(player)));
    }

    private OpenFireResult calculateOpenFireResult(Player player, DefaultGame cardGame) {
        String playerId = player.getPlayerId();
        int attackTotal = 0;
        for (PhysicalCard ship : _forces.get(player)) {
            attackTotal += ship.getBlueprint().getWeapons();
        }
        int defenseTotal = _targets.get(player).getBlueprint().getShields();
        cardGame.sendMessage(playerId + " opens fire");
        cardGame.sendMessage("ATTACK: " + attackTotal + ", DEFENSE: " + defenseTotal);
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
/* SelectForceEffect:
            return new ChooseCardsOnTableEffect(this, player,
                    "Choose ships to include in force", 1, getEligibleCardsForForce(player).size(),
                    getEligibleCardsForForce(player)) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    _forces.put(player, selectedCards);
                }
            }; */
//            return new SubAction(this, selectForceEffect(_attackingPlayer));
        }

        if (_targets.get(_attackingPlayer) == null) {
/*            return new ChooseCardsOnTableEffect(this, player,
                    "Choose target", 1, 1, getTargetOptions(player)) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                    _targets.put(player, Iterables.getOnlyElement(selectedCards));
                }
            };*/

//            return new SubAction(this, getTargetEffect(_attackingPlayer));
        }

        if (!_returnFireDecisionMade) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new YesNoDecision(_defendingPlayer, "Do you want to return fire?", cardGame) {
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
//            return new SubAction(this, selectForceEffect(_defendingPlayer));
        }

        if (_returningFire && _targets.get(_defendingPlayer) == null) {
//            return new SubAction(this, getTargetEffect(_defendingPlayer));
        }

        if (!_openedFire) {
            _openedFire = true;
            _openFireResults.put(_attackingPlayer, calculateOpenFireResult(_attackingPlayer, cardGame));
        }

        if (!_returnedFire && _returningFire) {
            _returnedFire = true;
            _openFireResults.put(_defendingPlayer, calculateOpenFireResult(_defendingPlayer, cardGame));
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
    public PhysicalCard getPerformingCard() {
        return _actionSource;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _actionSource.getCardId();
    }

    public boolean wasCarriedOut() {
        return _wasCarriedOut = true;
    }

}