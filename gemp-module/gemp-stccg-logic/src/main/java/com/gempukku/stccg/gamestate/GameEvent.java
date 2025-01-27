package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {
    public enum Type {
        PARTICIPANTS("P"), GAME_PHASE_CHANGE("GPC"), TURN_CHANGE("TC"),
        PUT_SHARED_MISSION_INTO_PLAY("PUT_SHARED_MISSION_INTO_PLAY"),
        TRIBBLE_SEQUENCE_UPDATE("TSEQ"),
        PLAYER_DECKED("PLAYER_DECKED"), // TODO: Not implemented in JavaScript
        PLAYER_SCORE("PLAYER_SCORE"), // TODO: Not implemented in JavaScript
        PUT_CARD_INTO_PLAY("PCIP"),
        PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING("PCIPAR"),
        MOVE_CARD_IN_PLAY("MCIP"), REMOVE_CARD_FROM_PLAY("RCFP"),
        SEND_MESSAGE("M"), SEND_WARNING("W"),
        GAME_STATS("GS"),
        CHAT_MESSAGE("CM"),
        GAME_ENDED("EG"),
        UPDATE_CARD_IMAGE("UPDATE_CARD_IMAGE"),
        CARD_AFFECTED_BY_CARD("CAC"), SHOW_CARD_ON_SCREEN("EP"), FLASH_CARD_IN_PLAY("CA"),
        DECISION("D"), GAME_STATE_CHECK("GAME_STATE_CHECK");

        private final String code;

        Type(String code) {
            this.code = code;
        }

    }

    public enum Attribute {
        /* Don't change these names without editing the client code, as it relies on the .name() method */
        allParticipantIds, blueprintId, cardId, controllerId, decisionType, discardPublic, id, imageUrl,
        locationIndex, message, otherCardIds, quadrant, participantId, phase, targetCardId, text, timestamp,
        type, zone,
        placedOnMission,
        region, serializedGameState
    }

    private final Type _type;
    private final DefaultGame _game;
    private Zone _zone;
    private GameState _gameState;
    private AwaitingDecision _awaitingDecision;
    private final Map<Attribute, String> _eventAttributes = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(GameEvent.class);

    public GameEvent(DefaultGame cardGame, Type type) {
        _type = type;
        _game = cardGame;
        _eventAttributes.put(Attribute.type, type.code);
        _eventAttributes.put(Attribute.timestamp,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS")));
    }


    public GameEvent(DefaultGame cardGame, Type type, Player player) {
        this(cardGame, type);
        _eventAttributes.put(Attribute.participantId, player.getPlayerId());
    }


    public GameEvent(DefaultGame cardGame, Type type, String message) {
        this(cardGame, type);
        _eventAttributes.put(Attribute.message, message);
    }

    public GameEvent(DefaultGame cardGame, Type type, PhysicalCard card) {
        this(cardGame, type, card.getOwner());
        setCardData(card);
    }


    public GameEvent(DefaultGame cardGame, Type type, Collection<PhysicalCard> cards, String playerId) {
        this(cardGame, type);
        setOtherCards(cards);
        _eventAttributes.put(Attribute.participantId, playerId);
    }

    public GameEvent(DefaultGame cardGame, Type type, Phase phase) {
        this(cardGame, type);
        _eventAttributes.put(Attribute.phase, phase.toString());
    }
    public GameEvent(Type type, PhysicalCard card, Player player) {
        this(card.getGame(), type, player);
        setCardData(card);
    }

    public GameEvent(DefaultGame cardGame, Type type, GameState gameState) {
        this(cardGame, type);
        _gameState = gameState;
    }

    public GameEvent(DefaultGame cardGame, Type type, GameState gameState, Player player) {
        this(cardGame, type, player);
        _eventAttributes.put(Attribute.allParticipantIds,
                String.join(",", (gameState.getPlayerOrder().getAllPlayers())));
        _eventAttributes.put(Attribute.discardPublic, String.valueOf(cardGame.getFormat().discardPileIsPublic()));
    }

    public GameEvent(DefaultGame cardGame, Type type, AwaitingDecision decision, Player player) {
        this(cardGame, type, player);
        _awaitingDecision = decision;
        _eventAttributes.put(Attribute.id, String.valueOf(decision.getDecisionId()));
        _eventAttributes.put(Attribute.decisionType, decision.getDecisionType().name());
        if (decision.getText() != null)
            _eventAttributes.put(Attribute.text, decision.getText());
        _eventAttributes.put(Attribute.phase, cardGame.getCurrentPhase().name());
    }

    private void setOtherCards(Collection<PhysicalCard> cards) {
        int[] otherCardIds = new int[cards.size()];
        int index = 0;
        for (PhysicalCard card : cards) {
            otherCardIds[index] = card.getCardId();
            index++;
        }
        _eventAttributes.put(Attribute.otherCardIds, TextUtils.arrayToCommaSeparated(otherCardIds));
    }

    private void setCardData(PhysicalCard card) {
        _eventAttributes.put(Attribute.cardId, String.valueOf(card.getCardId()));
        _eventAttributes.put(Attribute.blueprintId, card.getBlueprintId());
        _eventAttributes.put(Attribute.zone, card.getZone().name());
        _zone = card.getZone();
        _eventAttributes.put(Attribute.imageUrl, card.getImageUrl());
        _eventAttributes.put(Attribute.controllerId, card.getOwnerName()); // TODO - Owner, not controller

        // int locationZoneIndex
        if (card instanceof ST1EPhysicalCard stCard) {
            try {
                MissionLocation location = stCard.getLocation();
                int locationZoneIndex = location.getLocationZoneIndex(stCard.getGame());
                _eventAttributes.put(Attribute.locationIndex, String.valueOf(locationZoneIndex));
            } catch(InvalidGameLogicException | NullPointerException ignored) {
                // Don't serialize the location if the card doesn't have one yet
                // TODO - Eventually we'll be removing game events
            }
        }

        if (card.getCardType() == CardType.MISSION && card.isInPlay()) {
            try {
                _eventAttributes.put(Attribute.quadrant, card.getLocation().getQuadrant().name());
                if (card.getLocation().getRegion() != null)
                    _eventAttributes.put(Attribute.region, card.getLocation().getRegion().name());
            } catch(InvalidGameLogicException exp) {
                _game.sendErrorMessage(exp);
            }
        }

        if (card.getStackedOn() != null)
            _eventAttributes.put(Attribute.targetCardId, String.valueOf(card.getStackedOn().getCardId()));
        else if (card.getAttachedTo() != null)
            _eventAttributes.put(Attribute.targetCardId, String.valueOf(card.getAttachedTo().getCardId()));
        serializeGameState(_game.getGameState());
        if (card.isPlacedOnMission()) {
            try {
                _eventAttributes.put(Attribute.placedOnMission, "true");
                _eventAttributes.put(Attribute.targetCardId,
                        String.valueOf(card.getLocation().getTopMission().getCardId()));
            } catch(InvalidGameLogicException exp) {
                _game.sendErrorMessage(exp);
            }
        }
    }

    public Type getType() { return _type; }
    public Zone getZone() { return _zone; }

    public String getAttribute(Attribute attribute) { return _eventAttributes.get(attribute); }

    public Node serialize(Document doc) {
        Element eventElem = doc.createElement("ge");

        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }

        if (_gameState != null)
            serializeGameStats(doc, eventElem);
        if (_awaitingDecision != null)
            serializeDecision(doc, eventElem);

        return eventElem;
    }

    private void serializeDecision(Document doc, Element eventElem) {
        for (Map.Entry<String, String[]> paramEntry : _awaitingDecision.getDecisionParameters().entrySet()) {
            for (String value : paramEntry.getValue()) {
                Element decisionParam = doc.createElement("parameter");
                decisionParam.setAttribute("name", paramEntry.getKey());
                decisionParam.setAttribute("value", value);
                eventElem.appendChild(decisionParam);
            }
        }
        try {
            if (_awaitingDecision instanceof ArbitraryCardsSelectionDecision arbitrary) {
                if (arbitrary.getValidCombinations() != null) {
                    Element decisionParam = doc.createElement("parameter");
                    decisionParam.setAttribute("name", "combinations");
                    decisionParam.setAttribute("value", arbitrary.getValidCombinations());
                    eventElem.appendChild(decisionParam);
                }
            }
        } catch(Exception exp) {
            _game.sendMessage("Unable to process decision");
        }
    }

    private void serializeGameStats(Document doc, Element eventElem) {
        for (Map.Entry<String, Map<Zone, Integer>> playerZoneSizes : _game.getZoneSizes().entrySet()) {
            final Element playerZonesElem = doc.createElement("playerZones");

            playerZonesElem.setAttribute("name", playerZoneSizes.getKey());

            for (Map.Entry<Zone, Integer> zoneSizes : playerZoneSizes.getValue().entrySet())
                playerZonesElem.setAttribute(zoneSizes.getKey().name(), zoneSizes.getValue().toString());

            eventElem.appendChild(playerZonesElem);
        }

        for (Player player : _game.getPlayers()) {
            final Element playerScoreElem = doc.createElement("playerScores");
            playerScoreElem.setAttribute("name", player.getPlayerId());
            playerScoreElem.setAttribute("score", String.valueOf(player.getScore()));
            eventElem.appendChild(playerScoreElem);
        }

        StringBuilder charStr = new StringBuilder();
        if (!charStr.isEmpty())
            charStr.delete(0, 1);

        if (!charStr.isEmpty())
            eventElem.setAttribute("charStats", charStr.toString());
    }

    private void serializeGameState(GameState gameState) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            _eventAttributes.put(Attribute.serializedGameState, mapper.writeValueAsString(gameState));
        } catch(JsonProcessingException exp) {
            _game.sendMessage("Unable to create serialized game state");
            LOGGER.error("Unable to create serialized game state", exp);
        }
    }

}