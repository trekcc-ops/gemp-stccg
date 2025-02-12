package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SendGameStatsGameEvent extends GameEvent {

    private final Map<String, Map<Zone, Integer>> _playerZoneSizes;
    private final Collection<Player> _players;

    public SendGameStatsGameEvent(DefaultGame cardGame) {
        super(Type.GAME_STATS);
        _playerZoneSizes = cardGame.getZoneSizes();
        _players = cardGame.getPlayers();
    }

    public Node serialize(Document doc) {
        Element eventElem = doc.createElement("ge");

        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        serializeGameStats(doc, eventElem);
        return eventElem;
    }

    @JsonProperty("playerZones")
    private List<PlayerZoneSizes> getPlayerZoneSizes() {
        List<PlayerZoneSizes> result = new ArrayList<>();
        for (Player player : _players) {
            result.add(new PlayerZoneSizes(player));
        }
        return result;
    }

    @JsonProperty("playerScores")
    private List<PlayerScores> getPlayerScores() {
        List<PlayerScores> result = new ArrayList<>();
        for (Player player : _players) {
            result.add(new PlayerScores(player));
        }
        return result;
    }


    private class PlayerZoneSizes {

        final Player _zoneSizePlayer;

        PlayerZoneSizes(Player player) {
            _zoneSizePlayer = player;
        }

        @JsonProperty("playerId")
        String getPlayerId() {
            return _zoneSizePlayer.getPlayerId();
        }

        @JsonProperty("hand")
        int getCardsInHand() {
            return _zoneSizePlayer.getCardsInHand().size();
        }

        @JsonProperty("discard")
        int getCardsInDiscard() {
            return _zoneSizePlayer.getDiscardPile().size();
        }

        @JsonProperty("drawDeck")
        int getCardsInDrawDeck() {
            return _zoneSizePlayer.getCardsInDrawDeck().size();
        }

        @JsonProperty("removed")
        int getCardsInRemoved() {
            return _zoneSizePlayer.getRemovedPile().size();
        }

    }

    private class PlayerScores {

        final Player _scorePlayer;

        PlayerScores(Player player) {
            _scorePlayer = player;
        }

        @JsonProperty("playerId")
        String getPlayerId() {
            return _scorePlayer.getPlayerId();
        }

        @JsonProperty("score")
        int getScore() {
            return _scorePlayer.getScore();
        }

    }


    public void serializeGameStats(Document doc, Element eventElem) {
        for (Map.Entry<String, Map<Zone, Integer>> playerZoneSizes : _playerZoneSizes.entrySet()) {
            final Element playerZonesElem = doc.createElement("playerZones");

            playerZonesElem.setAttribute("name", playerZoneSizes.getKey());

            for (Map.Entry<Zone, Integer> zoneSizes : playerZoneSizes.getValue().entrySet())
                playerZonesElem.setAttribute(zoneSizes.getKey().name(), zoneSizes.getValue().toString());

            eventElem.appendChild(playerZonesElem);
        }

        for (Player player : _players) {
            final Element playerScoreElem = doc.createElement("playerScores");
            playerScoreElem.setAttribute("name", player.getPlayerId());
            playerScoreElem.setAttribute("score", String.valueOf(player.getScore()));
            eventElem.appendChild(playerScoreElem);
        }
    }


}