package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.discard.NullifyCardAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("MethodWithMultipleReturnPoints")
public abstract class AbstractAtTest implements UserInputSimulator {

    protected String P1 = "player1";
    protected String P2 = "player2";
    protected List<String> _players = List.of("player1", "player2");

    protected ST1EGame _game;
    protected static final CardBlueprintLibrary _cardLibrary = new CardBlueprintLibrary();
    protected FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);

    public DefaultGame getGame() {
        return _game;
    }


    protected boolean personnelAttributesAre(PersonnelCard personnel, List<Integer> attributeValues) {
        if (!Objects.equals(personnel.getIntegrity(_game), attributeValues.get(0))) {
            return false;
        }
        if (!Objects.equals(personnel.getCunning(_game), attributeValues.get(1))) {
            return false;
        }
        if (!Objects.equals(personnel.getStrength(_game), attributeValues.get(2))) {
            return false;
        }
        return true;
    }

    protected JsonNode getJsonForPerformedAction(GameState gameState, String requestingPlayerId, Action action)
            throws JsonProcessingException {
        String gameStateString = gameState.serializeForPlayer(requestingPlayerId);
        JsonNode gameStateJson = new ObjectMapper().readTree(gameStateString);
        for (JsonNode node : gameStateJson.get("performedActions")) {
            if (node.get("actionId").asInt() == action.getActionId()) {
                return node;
            }
        }
        throw new RuntimeException("Could not find JSON data for requested action");
    }

    protected JsonNode getJsonForSelectableAction(ST1EGameState gameState, String requestingPlayerId, Action action)
            throws JsonProcessingException {
        String gameStateString = gameState.serializeForPlayer(requestingPlayerId);
        JsonNode gameStateJson = new ObjectMapper().readTree(gameStateString);
        for (JsonNode node : gameStateJson.get("pendingDecision").get("actions")) {
            if (node.get("actionId").asInt() == action.getActionId()) {
                return node;
            }
        }
        throw new RuntimeException("Could not find JSON data for requested action");
    }

    protected boolean cardWasNullified(PhysicalCard card) {
        if (card.isInPlay()) {
            return false;
        }
        boolean actionFound = false;
        for (Action action : _game.getActionsEnvironment().getPerformedActions()) {
            if (action instanceof NullifyCardAction nullifyAction &&
                    nullifyAction.getNullifiedCard() == card) {
                actionFound = true;
                break;
            }
        }
        return actionFound;
    }

    protected boolean personnelWasKilled(PersonnelCard personnel) {
        if (personnel.getZone() != Zone.DISCARD) {
            return false;
        }
        boolean actionFound = false;
        for (Action action : _game.getActionsEnvironment().getPerformedActions()) {
            if (action instanceof KillSinglePersonnelAction killAction &&
                    killAction.getVictimCard() == personnel) {
                actionFound = true;
                break;
            }
        }
        return actionFound;
    }

    protected boolean selectableCardsAre(Collection<? extends PhysicalCard> expectedCards, String playerName) {
        try {
            Collection<? extends PhysicalCard> selectableCards = getSelectableCards(playerName);
            if (selectableCards.size() != expectedCards.size()) {
                return false;
            } else {
                return selectableCards.containsAll(expectedCards);
            }
        } catch(DecisionResultInvalidException exp) {
            return false;
        }
    }

}