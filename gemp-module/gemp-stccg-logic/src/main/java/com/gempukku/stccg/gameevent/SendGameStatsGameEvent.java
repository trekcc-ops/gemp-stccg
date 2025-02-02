package com.gempukku.stccg.gameevent;

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

    // Fix the freakin charstats thing

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

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "playerZones")
    private List<PlayerZoneSizes> getPlayerZoneSizes() {
        List<PlayerZoneSizes> result = new ArrayList<>();
        for (Player player : _players) {
            result.add(new PlayerZoneSizes(player));
        }
        return result;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "playerScores")
    private List<PlayerScores> getPlayerScores() {
        List<PlayerScores> result = new ArrayList<>();
        for (Player player : _players) {
            result.add(new PlayerScores(player));
        }
        return result;
    }


    @JacksonXmlRootElement(localName = "playerZones")
    private class PlayerZoneSizes {

        final Player _zoneSizePlayer;

        PlayerZoneSizes(Player player) {
            _zoneSizePlayer = player;
        }

        @JacksonXmlProperty(localName = "name", isAttribute = true)
        String getPlayerId() {
            return _zoneSizePlayer.getPlayerId();
        }

        @JacksonXmlProperty(localName = "HAND", isAttribute = true)
        int getCardsInHand() {
            return _zoneSizePlayer.getCardsInHand().size();
        }

        @JacksonXmlProperty(localName = "DISCARD", isAttribute = true)
        int getCardsInDiscard() {
            return _zoneSizePlayer.getDiscardPile().size();
        }

        @JacksonXmlProperty(localName = "DRAW_DECK", isAttribute = true)
        int getCardsInDrawDeck() {
            return _zoneSizePlayer.getCardsInDrawDeck().size();
        }

        @JacksonXmlProperty(localName = "REMOVED", isAttribute = true)
        int getCardsInRemoved() {
            return _zoneSizePlayer.getRemovedPile().size();
        }

    }

    @JacksonXmlRootElement(localName = "playerScores")
    private class PlayerScores {

        final Player _scorePlayer;

        PlayerScores(Player player) {
            _scorePlayer = player;
        }

        @JacksonXmlProperty(localName = "name", isAttribute = true)
        String getPlayerId() {
            return _scorePlayer.getPlayerId();
        }

        @JacksonXmlProperty(localName = "score", isAttribute = true)
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