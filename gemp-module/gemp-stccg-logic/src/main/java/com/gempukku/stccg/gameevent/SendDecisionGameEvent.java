package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
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
import java.util.List;
import java.util.Map;

public class SendDecisionGameEvent extends GameEvent {

    private final AwaitingDecision _decision;
    private final Phase _currentPhase;

    public SendDecisionGameEvent(DefaultGame cardGame, AwaitingDecision decision, Player decidingPlayer) {
        super(Type.DECISION, decidingPlayer);
        _currentPhase = cardGame.getCurrentPhase();
        _decision = decision;
    }

    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private String getDecisionId() {
        return String.valueOf(_decision.getDecisionId());
    }

    @JacksonXmlProperty(localName = "decisionTpe", isAttribute = true)
    private AwaitingDecisionType getDecisionType() {
        return _decision.getDecisionType();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlProperty(localName = "text", isAttribute = true)
    private String getDecisionText() {
        return _decision.getText();
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "parameter")
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

    @JacksonXmlProperty(localName = "phase", isAttribute = true)
    private Phase getCurrentPhase() {
        return _currentPhase;
    }

    @Override
    public Node serialize(Document doc) throws JsonProcessingException {
        Element eventElem = doc.createElement("ge");
/*        for (Attribute attribute : _eventAttributes.keySet()) {
            if (getAttribute(attribute) != null)
                eventElem.setAttribute(attribute.name(), getAttribute(attribute));
        }
        serializeDecision(doc, eventElem); */
        return eventElem;
    }

    @JacksonXmlRootElement(localName = "parameter")
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