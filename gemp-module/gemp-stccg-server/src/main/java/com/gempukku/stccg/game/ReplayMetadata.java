package com.gempukku.stccg.game;

import com.gempukku.stccg.DBDefs;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.common.CardDeck;
import com.mysql.cj.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ReplayMetadata {

    public static class DeckMetadata {
        public String Owner;
        public String TargetFormat;
        public String DeckName;
        public List<String> DrawDeck;
    }

    public final DBDefs.GameHistory GameReplayInfo;

    public final Map<String, DeckMetadata> Decks = new HashMap<>();
    public final Map<String, Integer> PlayerIDs = new HashMap<>();
    public final Map<String, Integer> Bids = new HashMap<>();
    public String WentFirst;
    public boolean GameStarted = false;

    public final Map<String, String> AllCards = new HashMap<>();

    public final Set<String> SeenCards = new HashSet<>();

    public final HashSet<String> PlayedCards = new HashSet<>();

    public ReplayMetadata(DBDefs.GameHistory game, Map<String, CardDeck> decks) {
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

            Decks.put(player, metadata);
        }

    }

    public ReplayMetadata(DBDefs.GameHistory game, Map<String, CardDeck> decks, String player, List<GameEvent> events) {
        this(game, decks);
        ParseReplay(player, events);
    }

    public String GetOpponent(String player) {
        return PlayerIDs.keySet().stream().filter(x -> !x.equals(player)).findFirst().orElse(null);
    }

    private final Pattern gameStartPattern = Pattern.compile("Players in the game are: ([\\w-]+), ([\\w-]+)");
    private final Pattern orderPattern = Pattern.compile("([\\w-]+) has chosen to go (.*)");
    private final Pattern bidPattern = Pattern.compile("([\\w-]+) bid (\\d+)");
    public void ParseReplay(String player, List<GameEvent> events) {
        GameStarted = false;

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
                    if(order.equals("first")) {
                        WentFirst = bidder;
                    }
                    else if(order.equals("second")) {
                        WentFirst = GetOpponent(bidder);
                    }
                    continue;
                }

                regex = bidPattern.matcher(message);
                if(regex.matches()) {
                    String bidder = regex.group(1);
                    String bid = regex.group(2);
                    Bids.put(bidder, Integer.valueOf(bid));
                }
            }
            else if(!GameStarted && event.getType() == GameEvent.Type.GAME_PHASE_CHANGE) {
                var phase = Phase.findPhase(event.getAttribute(GameEvent.Attribute.phase));
                if (phase == Phase.BETWEEN_TURNS)
                {
                    GameStarted = true;
                }
            }

            else if(event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY) {

                var bpID = event.getAttribute(GameEvent.Attribute.blueprintId);
                var cardId = event.getAttribute(GameEvent.Attribute.cardId);
                var participantId = event.getAttribute(GameEvent.Attribute.participantId);
                var zone = event.getZone();

                if (bpID != null && cardId != null && participantId != null && participantId.equals(player)) {
                    AllCards.put(cardId, bpID);
                    SeenCards.add(cardId);
                    if (zone.isInPlay())
                        PlayedCards.add(cardId);

                }
            }
        }
    }
}
