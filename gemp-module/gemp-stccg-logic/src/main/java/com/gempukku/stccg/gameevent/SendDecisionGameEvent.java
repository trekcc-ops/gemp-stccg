package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SendDecisionGameEvent extends GameEvent {

    private final AwaitingDecision _decision;

    public SendDecisionGameEvent(DefaultGame cardGame, AwaitingDecision decision, Player decidingPlayer) {
        super(cardGame,Type.DECISION, decidingPlayer);
        _decision = decision;
        _eventAttributes.put(Attribute.id, String.valueOf(decision.getDecisionId()));
        _eventAttributes.put(Attribute.decisionType, decision.getDecisionType().name());
        if (decision.getText() != null)
            _eventAttributes.put(Attribute.text, decision.getText());
        _eventAttributes.put(Attribute.phase, cardGame.getCurrentPhase().name());
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

}