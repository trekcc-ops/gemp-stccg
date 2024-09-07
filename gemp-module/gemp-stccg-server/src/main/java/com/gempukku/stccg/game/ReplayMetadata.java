package com.gempukku.stccg.game;

import com.gempukku.stccg.common.DBDefs;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.cards.CardDeck;
import com.mysql.cj.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ReplayMetadata {

    public static class DeckMetadata {
        public String Owner;
        public String TargetFormat;
        public String DeckName;
        public List<String> DrawDeck;
        public String Ring;
        public final List<String> StartingFellowship = new ArrayList<>();
    }

    //Version 1: First tracked version; original version was completely different
    //Version 2: Adding the highest achieved sites by player, game IDs, and game timer length information
    public Integer MetadataVersion = 2;

    public final DBDefs.GameHistory GameReplayInfo;

    public final Map<String, DeckMetadata> Decks = new HashMap<>();
    public final Map<String, Integer> PlayerIDs = new HashMap<>();
    public final Map<String, Integer> Bids = new HashMap<>();
    public String WentFirst;
    public boolean GameStarted = false;
    public boolean Conceded = false;
    public boolean Canceled = false;

    public final Map<String, String> AllCards = new HashMap<>();

    public final Set<Integer> SeenCards = new HashSet<>();

    public final HashSet<Integer> PlayedCards = new HashSet<>();

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

        if(GameReplayInfo.lose_reason.contains("cancelled") || GameReplayInfo.win_reason.contains("cancelled")) {
            Canceled = true;
        }

        if(GameReplayInfo.lose_reason.contains("Concession") || GameReplayInfo.win_reason.contains("Concession")) {
            Conceded = true;
        }
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
                var message = event.getMessage();
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
                var phase = Phase.findPhase(event.getPhase());
                if (phase == Phase.BETWEEN_TURNS)
                {
                    GameStarted = true;
                }
            }

            else if(event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY) {

                var bpID = event.getBlueprintId();
                var cardID = event.getCardId();
                var participantID = event.getParticipantId();
                var zone = event.getZone();

                if (bpID != null && cardID != null && participantID != null && participantID.equals(player)) {
                    AllCards.put(cardID.toString(), bpID);
                    SeenCards.add(cardID);
                    if (zone.isInPlay())
                        PlayedCards.add(cardID);

                }
            }
        }
    }
}
