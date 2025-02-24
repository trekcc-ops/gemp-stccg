package com.gempukku.stccg.game;

import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gameevent.GameEvent;
import com.gempukku.stccg.common.CardDeck;
import com.mysql.cj.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ReplayMetadata {

    @SuppressWarnings("unused") // Class accessed via JSON which may not be obvious to IDE
    public static class DeckMetadata {
        public String Owner;
        public String TargetFormat;
        public String DeckName;
        public List<String> DrawDeck;
    }

    @SuppressWarnings("unused")
    public final DBData.GameHistory GameReplayInfo; // Class accessed via JSON which may not be obvious to IDE
    private final Map<String, Integer> PlayerIDs = new HashMap<>();
    @SuppressWarnings("unused") // Class accessed via JSON which may not be obvious to IDE
    public String WentFirst;

    private final Map<String, String> AllCards = new HashMap<>();

    private final Collection<String> SeenCards = new HashSet<>();

    private final Collection<String> PlayedCards = new HashSet<>();

    private ReplayMetadata(DBData.GameHistory game, Map<String, ? extends CardDeck> decks) {
        GameReplayInfo = game;

        for(var pair : decks.entrySet()) {
            String player = pair.getKey();
            var deck = pair.getValue();
            var metadata = new DeckMetadata() {{
                Owner = player;
                TargetFormat = deck.getTargetFormat();
                DeckName = deck.getDeckName();
                DrawDeck = deck.getDrawDeckCards();
            }};

            Map<String, DeckMetadata> decks1 = new HashMap<>();
            decks1.put(player, metadata);
        }

    }

    public ReplayMetadata(DBData.GameHistory game, Map<String, ? extends CardDeck> decks, String player,
                          Iterable<? extends GameEvent> events) throws InvalidGameOperationException {
        this(game, decks);
        ParseReplay(player, events);
    }

    private final String GetOpponent(String player) {
        return PlayerIDs.keySet().stream().filter(x -> !x.equals(player)).findFirst().orElse(null);
    }

    private final Pattern gameStartPattern = Pattern.compile("Players in the game are: ([\\w-]+), ([\\w-]+)");
    private final Pattern orderPattern = Pattern.compile("([\\w-]+) has chosen to go (.*)");

    private final void ParseReplay(String player, Iterable<? extends GameEvent> events) throws InvalidGameOperationException {
        boolean gameStarted = false;

        for(var event : events) {
            if(event.getType() == GameEvent.Type.SEND_MESSAGE) {
                var message = event.getAttribute(GameEvent.Attribute.message);
                if(StringUtils.isNullOrEmpty(message))
                    continue;

                var regex = gameStartPattern.matcher(message);
                if(regex.matches()) {
                    PlayerIDs.put(regex.group(1), 1);
                    PlayerIDs.put(regex.group(2), 2);
                    continue;
                }

                regex = orderPattern.matcher(message);
                if(regex.matches()) {
                    String bidder = regex.group(1);
                    String order = regex.group(2);
                    if("first".equals(order)) {
                        WentFirst = bidder;
                    }
                    else if("second".equals(order)) {
                        WentFirst = GetOpponent(bidder);
                    }
                }
            }
            else if(!gameStarted) {
                var phase = Phase.findPhase(event.getAttribute(GameEvent.Attribute.phase));
                if (phase == Phase.BETWEEN_TURNS)
                {
                    gameStarted = true;
                }
            }
        }
    }
}