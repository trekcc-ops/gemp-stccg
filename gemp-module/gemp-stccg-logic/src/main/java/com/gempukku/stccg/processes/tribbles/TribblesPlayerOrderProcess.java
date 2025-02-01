package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TribblesPlayerOrderProcess extends TribblesGameProcess {

    public TribblesPlayerOrderProcess(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {

        LinkedList<String> playersInOrder = new LinkedList<>(_game.getPlayerIds());
        Collections.shuffle(playersInOrder, ThreadLocalRandom.current());

        Map<String, Integer> startingTribbles = new LinkedHashMap<>();
        LinkedList<String> playersSelecting = new LinkedList<>(playersInOrder);
        Map<String, Integer> playersWithOneValue = new LinkedHashMap<>();

        for (String playerId : playersSelecting) {
            Player player = cardGame.getPlayer(playerId);
            playersWithOneValue.put(playerId, playerOnlyHasOneTribbleValue(player));
        }

        while (playersSelecting.size() > 1) {
            startingTribbles.clear();

            for (String player : playersSelecting) {
                List<PhysicalCard> deckCards = _game.getGameState().getZoneCards(cardGame.getPlayer(player), Zone.DRAW_DECK);
                PhysicalCard randomCard = deckCards.get(new Random().nextInt(deckCards.size()));
                CardBlueprint randomBlueprint = randomCard.getBlueprint();
                _game.sendMessage(player + " drew " + randomBlueprint.getTitle());
                int randomTribbleCount = randomBlueprint.getTribbleValue();
                startingTribbles.put(player, randomTribbleCount);
            }

            int highestTribble = Collections.max(startingTribbles.values());

            boolean infiniteLoopPossible = true;

            for (Map.Entry<String, Integer> entry : playersWithOneValue.entrySet()) {
                if (entry.getValue() != highestTribble) {
                    infiniteLoopPossible = false;
                    break;
                }
            }

            for (String player : playersSelecting) {
                if (startingTribbles.get(player) < highestTribble) {
                    playersSelecting.remove(player);
                    playersWithOneValue.remove(player);
                }
            }

            /* If all remaining players have only one tribble value in their deck, and it's the same,
                    choose the first player at random. */
            if (infiniteLoopPossible) {
                Collections.shuffle(playersSelecting, ThreadLocalRandom.current());
                playersSelecting.subList(1, playersSelecting.size()).clear();
            }
        }
        _game.initializePlayerOrder(new PlayerOrder(playersInOrder));
    }
    
    private Integer playerOnlyHasOneTribbleValue(Player player) {
        ArrayList<Integer> uniqueValues = new ArrayList<>();
        for (PhysicalCard card : player.getCardsInDrawDeck()) {
            int value = card.getBlueprint().getTribbleValue();
            if (!uniqueValues.contains(value))
                uniqueValues.add(value);
        }
        if (uniqueValues.size() == 1)
            return uniqueValues.getFirst();
        else return 0;
    }
    
    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return new TribblesStartOfRoundGameProcess(_game);
    }
}