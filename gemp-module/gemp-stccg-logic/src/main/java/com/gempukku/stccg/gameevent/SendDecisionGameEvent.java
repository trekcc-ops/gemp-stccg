package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendDecisionGameEvent extends GameEvent {

    private final AwaitingDecision _decision;
    private final Phase _currentPhase;

    public SendDecisionGameEvent(DefaultGame cardGame, AwaitingDecision decision, Player decidingPlayer) {
        super(Type.DECISION, decidingPlayer);
        _currentPhase = cardGame.getCurrentPhase();
        _decision = decision;
        _eventAttributes.put(Attribute.id, String.valueOf(decision.getDecisionId()));
        _eventAttributes.put(Attribute.decisionType, decision.getDecisionType().name());
        if (decision.getText() != null)
            _eventAttributes.put(Attribute.text, decision.getText());
        _eventAttributes.put(Attribute.phase, cardGame.getCurrentPhase().name());
    }

    @JsonProperty("decisionId")
    private String getDecisionId() {
        return String.valueOf(_decision.getDecisionId());
    }

    @JsonProperty("decisionType")
    private AwaitingDecisionType getDecisionType() {
        return _decision.getDecisionType();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("text")
    private String getDecisionText() {
        return _decision.getText();
    }

    private List<DecisionParameter> getParameters() throws JsonProcessingException {
        List<DecisionParameter> result = new ArrayList<>();
        for (Map.Entry<String, String[]> paramEntry : _decision.getDecisionParameters().entrySet()) {
            for (String value : paramEntry.getValue()) {
                result.add(new DecisionParameter(paramEntry.getKey(), value));
            }
        }
        if (_decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            if (arbitrary.getValidCombinations() != null) {
                result.add(new DecisionParameter("combinations", arbitrary.getValidCombinations()));
            }
        }
        return result;
    }

    @JsonProperty("min")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String getMinimum() {
        if (_decision.getDecisionParameters().get("min") != null)
            return _decision.getDecisionParameters().get("min")[0];
        else return null;
    }

    @JsonProperty("max")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String getMaximum() {
        if (_decision.getDecisionParameters().get("max") != null)
            return _decision.getDecisionParameters().get("max")[0];
        else return null;
    }

    @JsonProperty("defaultValue")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String getDefaultValue() {
        if (_decision.getDecisionParameters().get("defaultValue") != null)
            return _decision.getDecisionParameters().get("defaultValue")[0];
        else return null;
    }

    @JsonProperty("results")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String[] getResults() {
        return _decision.getDecisionParameters().get("results");
    }
    
    @JsonProperty("cardIds")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String[] getCardIds() {
        return _decision.getDecisionParameters().get("cardId");
    }

    @JsonProperty("displayedCards")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Map<Object, Object>> getDisplayedCards() {
        if (_decision.getDecisionType() == AwaitingDecisionType.ARBITRARY_CARDS || 
                _decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION_FROM_COMBINATIONS) {
            List<Map<Object, Object>> result = new ArrayList<>();
            for (int i = 0; i < _decision.getDecisionParameters().get("cardId").length; i++) {
                Map<Object, Object> mapToAdd = new HashMap<>();
                mapToAdd.put("cardId", _decision.getDecisionParameters().get("cardId")[i]);
                mapToAdd.put("blueprintId", _decision.getDecisionParameters().get("blueprintId")[i]);
                mapToAdd.put("imageUrl", _decision.getDecisionParameters().get("imageUrl")[i]);
                mapToAdd.put("selectable", _decision.getDecisionParameters().get("selectable")[i]);
                result.add(mapToAdd);
            }
            return result;
        } else if (_decision.getDecisionType() == AwaitingDecisionType.ACTION_CHOICE) {
            List<Map<Object, Object>> result = new ArrayList<>();
            for (int i = 0; i < _decision.getDecisionParameters().get("cardId").length; i++) {
                Map<Object, Object> mapToAdd = new HashMap<>();
                mapToAdd.put("cardId", _decision.getDecisionParameters().get("cardId")[i]);
                mapToAdd.put("blueprintId", _decision.getDecisionParameters().get("blueprintId")[i]);
                mapToAdd.put("actionId", _decision.getDecisionParameters().get("actionId")[i]);
                mapToAdd.put("actionText", _decision.getDecisionParameters().get("actionText")[i]);
                result.add(mapToAdd);
            }
            return result;
        } else if (_decision.getDecisionType() == AwaitingDecisionType.CARD_ACTION_CHOICE) {
            List<Map<Object, Object>> result = new ArrayList<>();
            for (int i = 0; i < _decision.getDecisionParameters().get("cardId").length; i++) {
                Map<Object, Object> mapToAdd = new HashMap<>();
                mapToAdd.put("cardId", _decision.getDecisionParameters().get("cardId")[i]);
                mapToAdd.put("blueprintId", _decision.getDecisionParameters().get("blueprintId")[i]);
                mapToAdd.put("actionId", _decision.getDecisionParameters().get("actionId")[i]);
                mapToAdd.put("actionText", _decision.getDecisionParameters().get("actionText")[i]);
                mapToAdd.put("actionType", _decision.getDecisionParameters().get("actionType")[i]);
                result.add(mapToAdd);
            }
            return result;
        } else {
            return null;
        }
    }
    
    @JsonProperty("noPass")
    String getNoPass() {
        if (_decision.getDecisionParameters().get("noPass") != null)
            return _decision.getDecisionParameters().get("noPass")[0];
        else return null;
    }

    @JsonProperty("validCombinations")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, List<String>> getValidCombinations() {
        if (_decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            return arbitrary.getValidCombinationsMap();
        } else {
            return null;
        }
    }





    @Override
    public Node serialize(Document doc) throws JsonProcessingException {
        Element eventElem = doc.createElement("ge");
        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        serializeDecision(doc, eventElem);
        return eventElem;
    }

    @JsonProperty("phase")
    private Phase getCurrentPhase() {
        return _currentPhase;
    }


    private void serializeDecision(Document doc, Element eventElem) throws JsonProcessingException {
        for (Map.Entry<String, String[]> paramEntry : _decision.getDecisionParameters().entrySet()) {
            for (String value : paramEntry.getValue()) {
                Element decisionParam = doc.createElement("parameter");
                decisionParam.setAttribute("name", paramEntry.getKey());
                decisionParam.setAttribute("value", value);
                eventElem.appendChild(decisionParam);
            }
        }
        if (_decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            if (arbitrary.getValidCombinations() != null) {
                Element decisionParam = doc.createElement("parameter");
                decisionParam.setAttribute("name", "combinations");
                decisionParam.setAttribute("value", arbitrary.getValidCombinations());
                eventElem.appendChild(decisionParam);
            }
        }
    }

    private class DecisionParameter {

        @JacksonXmlProperty(localName = "name", isAttribute = true)
        String _name;
        @JacksonXmlProperty(localName = "value", isAttribute = true)
        String _value;

        DecisionParameter(String key, String valueElement) {
            _name = key;
            _value = valueElement;
        }
    }

}