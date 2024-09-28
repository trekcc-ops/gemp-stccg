package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.PlayerOrderFeedback;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TribblesPlayerOrderProcess extends GameProcess {
    private final Map<String, Integer> _startingTribbles = new LinkedHashMap<>();
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final LinkedList<String> _players = new LinkedList<>();
    private final Map<String, CardDeck> _decks;
    private final CardBlueprintLibrary _library;
    private String _firstPlayer;
    private final TribblesGame _game;

    public TribblesPlayerOrderProcess(Map<String, CardDeck> decks, CardBlueprintLibrary library,
                                      PlayerOrderFeedback playerOrderFeedback, TribblesGame game) {
        _players.addAll(decks.keySet());
        Collections.shuffle(_players, ThreadLocalRandom.current());
        _decks = decks;
        _playerOrderFeedback = playerOrderFeedback;
        _library = library;
        _game = game;
    }

    @Override
    public void process() {
        LinkedList<String> playersSelecting = new LinkedList<>(_players);
        Map<String, Integer> playersWithOneValue = new LinkedHashMap<>();

        for (String player: playersSelecting) {
            playersWithOneValue.put(player, playerOnlyHasOneTribbleValue(player));
        }

        while (playersSelecting.size() > 1) {
            _startingTribbles.clear();

            for (String player : playersSelecting) {
                List<String> playerDeckCards = _decks.get(player).getDrawDeckCards();
                String randomCard = playerDeckCards.get(new Random().nextInt(playerDeckCards.size()));
                try {
                    CardBlueprint randomBlueprint = _library.getCardBlueprint(randomCard);
                    _game.sendMessage(player + " drew " + randomBlueprint.getTitle());
                    int randomTribbleCount = randomBlueprint.getTribbleValue();
                    _startingTribbles.put(player, randomTribbleCount);
                } catch (CardNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            int highestTribble = Collections.max(_startingTribbles.values());

            boolean infiniteLoopPossible = true;

            for (Map.Entry<String, Integer> entry : playersWithOneValue.entrySet()) {
                if (entry.getValue() != highestTribble) {
                    infiniteLoopPossible = false;
                    break;
                }
            }

            for (String player : playersSelecting) {
                if (_startingTribbles.get(player) < highestTribble) {
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

        _firstPlayer = playersSelecting.getFirst();

        _playerOrderFeedback.setPlayerOrder(new PlayerOrder(_players), _firstPlayer);
    }

    private Integer playerOnlyHasOneTribbleValue(String playerId) {
        ArrayList<Integer> uniqueValues = new ArrayList<>();
        for (String card : _decks.get(playerId).getDrawDeckCards()) {
            try {
                Integer value = _library.getCardBlueprint(card).getTribbleValue();
                if (!uniqueValues.contains(value))
                    uniqueValues.add(value);
            } catch (CardNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (uniqueValues.size() == 1)
            return uniqueValues.getFirst();
        else return 0;
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesStartOfRoundGameProcess(_firstPlayer, _game);
    }
}
