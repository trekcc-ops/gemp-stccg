package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class TribblesEndOfRoundGameProcess extends TribblesGameProcess {
    public TribblesEndOfRoundGameProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException, InvalidGameLogicException {

        Map<String, Integer> pointsScored = new HashMap<>();
        TribblesGameState gameState = _game.getGameState();

        for (Player player : _game.getPlayers()) {
            String playerId = player.getPlayerId();

            // Count the total number of Tribbles in the play piles of the players who "went out" and score points.
            if (player.getCardsInHand().isEmpty()) {
                int score = calculateScore(gameState.getPlayPile(playerId));
                pointsScored.put(playerId, score);
                ScorePointsAction scorePointsAction = new ScorePointsAction(_game, null, player, score);
                scorePointsAction.processEffect(_game);
                _game.getActionsEnvironment().logCompletedActionNotInStack(scorePointsAction);
                _game.sendActionResultToClient(); // for updated points
                    // TODO - This doesn't work because we removed emitEffectResult
//                _game.getActionsEnvironment().emitEffectResult(new PlayerWentOutResult(playerId));
            }

            // Each player then shuffles their play pile into their decks.
            ShuffleCardsIntoDrawDeckAction action =
                    new ShuffleCardsIntoDrawDeckAction(_game, null, playerId,
                            new InCardListFilter(player.getCardGroupCards(Zone.PLAY_PILE)));
            action.processEffect(cardGame);
            cardGame.getActionsEnvironment().logCompletedActionNotInStack(action);
            cardGame.sendActionResultToClient();
        }

        ((ModifiersLogic) _game.getModifiersEnvironment()).signalEndOfRound();

        if (gameState.isLastRound()) {
            Map<String, Integer> finalPoints = new HashMap<>();
            for (Player player : _game.getPlayers()) {
                finalPoints.put(player.getPlayerId(), player.getScore());
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
            int lowestScore = Collections.min(pointsScored.values());
            pointsScored.entrySet().removeIf(entry -> entry.getValue() > lowestScore);
            List<String> firstPlayerList = new ArrayList<>(pointsScored.keySet());

                /* In most games, there will be a clear first player at this point. For cases where multiple
                    players went out with the same lowest score, this code will randomly select one to be the
                    first player. Tribbles rules are inconclusive about what should happen in this case.
                 */
            String firstPlayer = firstPlayerList.get(new Random().nextInt(firstPlayerList.size()));

            gameState.setCurrentPlayerId(firstPlayer);
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
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new TribblesStartOfRoundGameProcess(_game);
    }
}