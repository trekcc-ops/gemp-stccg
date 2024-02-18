package com.gempukku.stccg.processes;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.PlayerWentOutResult;

import java.util.*;

public class TribblesEndOfRoundGameProcess extends GameProcess {
    private final Map<String, Integer> _pointsScored = new HashMap<>();
    private GameProcess _nextProcess;
    private TribblesGame _game;
    TribblesEndOfRoundGameProcess(TribblesGame game) {
        super();
        _game = game;
    }
    @Override
    public void process() {
        
        TribblesGameState gameState = _game.getGameState();

        for (String playerId : _game.getPlayerIds()) {

            // Count the total number of Tribbles in the play piles of the players who "went out" and score points.
            if (gameState.getHand(playerId).isEmpty()) {
                gameState.playerWentOut(playerId); // TODO: Nothing specifically implemented for this code
                int score = calculateScore(gameState.getPlayPile(playerId));
                _pointsScored.put(playerId, score);
                gameState.addToPlayerScore(playerId, score);
                gameState.sendMessage(playerId + " went out with " + score + " points");
                _game.getActionsEnvironment().emitEffectResult(new PlayerWentOutResult(playerId));
            }

            // Each player places the cards remaining in their hand into their discard pile.
            gameState.discardHand(_game, playerId);

            // Each player then shuffles their play pile into their decks.
            gameState.shufflePlayPileIntoDeck(_game, playerId);
        }

        ((ModifiersLogic) _game.getModifiersEnvironment()).signalEndOfRound();

        if (gameState.isLastRound()) {
            Map<String, Integer> finalPoints = new HashMap<>();
            for (String playerId : _game.getPlayerIds()) {
                finalPoints.put(playerId, _game.getGameState().getPlayerScore(playerId));
            }
            int highestScore = Collections.max(finalPoints.values());
            finalPoints.entrySet().removeIf(entry -> entry.getValue() < highestScore);
                /* Winner is randomly chosen from tied players in case of tie. This is very much not the way
                    that Tribbles CCG works, but is a temporary solution as current code does not have a way to
                    end a game in a tie. (It is also very unlikely to end a Tribbles game in a tie.)
                 */
            List<String> winningPlayerList = new ArrayList<>(finalPoints.keySet());
            String winningPlayer = winningPlayerList.get(new Random().nextInt(winningPlayerList.size()));
            _game.playerWon(winningPlayer, "highest score after 5 rounds");
        } else {
            /* The player who "went out" this round will take the first turn in the next round.
                If multiple players "went out" in the previous round, the player who "went out" with the
                lowest points scored will play first. */
            int lowestScore = Collections.min(_pointsScored.values());
            _pointsScored.entrySet().removeIf(entry -> entry.getValue() > lowestScore);
            List<String> firstPlayerList = new ArrayList<>(_pointsScored.keySet());

                /* In most games, there will be a clear first player at this point. For cases where multiple
                    players went out with the same lowest score, this code will randomly select one to be the
                    first player. Tribbles rules are inconclusive about what should happen in this case.
                 */
            String firstPlayer = firstPlayerList.get(new Random().nextInt(firstPlayerList.size()));
            gameState.sendMessage("DEBUG: " + firstPlayer + " will go first next round.");
            _nextProcess = new TribblesStartOfRoundGameProcess(firstPlayer, _game);
        }
    }

    public int calculateScore(List<PhysicalCard> playPile) {
        int score = 0;
        for (PhysicalCard card : playPile) {
            score += card.getBlueprint().getTribbleValue();
        }
        return score;
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}