package com.gempukku.stccg;

import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.*;

public class GenericCardTestHelper extends AbstractAtTest {

    // Player key, then name/card
    public final Map<String, Map<String, PhysicalCard>> Cards = new HashMap<>();


    public PhysicalCard GetCard(String player, String cardName) { return Cards.get(player).get(cardName); }

    public List<String> GetAvailableActions(String playerID) {
        AwaitingDecision decision = GetAwaitingDecision(playerID);
        if(decision == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(decision.getDecisionParameters().get("actionText"));
    }

    public AwaitingDecision GetAwaitingDecision(String playerID) { return _userFeedback.getAwaitingDecision(playerID); }

    public Boolean DecisionAvailable(String playerID, String text)
    {
        AwaitingDecision ad = GetAwaitingDecision(playerID);
        if(ad == null)
            return false;
        String lowerText = text.toLowerCase();
        return ad.getText().toLowerCase().contains(lowerText);
    }

    public Boolean ActionAvailable(String player, String action) {
        List<String> actions = GetAvailableActions(player);
        if(actions == null)
            return false;
        String lowerAction = action.toLowerCase();
        return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerAction));
    }

    public Boolean ChoiceAvailable(String player, String choice) {
        List<String> actions = GetADParamAsList(player, "results");
        if(actions == null)
            return false;
        String lowerChoice = choice.toLowerCase();
        return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerChoice));
    }

    public Boolean AnyActionsAvailable(String player) {
        List<String> actions = GetAvailableActions(player);
        return !actions.isEmpty();
    }

    public Boolean AnyDecisionsAvailable(String player) {
        AwaitingDecision ad = GetAwaitingDecision(player);
        return ad != null;
    }

    public List<String> GetADParamAsList(String playerID, String paramName) { return Arrays.asList(GetAwaitingDecisionParam(playerID, paramName)); }
    public String[] GetAwaitingDecisionParam(String playerID, String paramName) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters().get(paramName);
    }

    public Map<String, String[]> GetAwaitingDecisionParams(String playerID) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters();
    }

    public Phase GetCurrentPhase() { return _game.getGameState().getCurrentPhase(); }


    public void ChooseCardIDFromSelection(String player, PhysicalCard...cards) throws DecisionResultInvalidException, InvalidGameOperationException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        //playerDecided(player, "" + card.getCardId());

        String[] choices = GetAwaitingDecisionParam(player,"cardId");
        Collection<String> ids = new ArrayList<>();
        Collection<PhysicalCard> found = new ArrayList<>();

        for (String choice : choices) {
            for (PhysicalCard card : cards) {
                if (found.contains(card))
                    continue;

                if ((String.valueOf(card.getCardId())).equals(choice)) {
                    ids.add(choice);
                    found.add(card);
                    break;
                }
            }
        }

        playerDecided(player, String.join(",", ids));
    }


    public void ApplyAdHocModifier(Modifier mod)
    {
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(mod);
    }

    public void ApplyAdHocAction(ActionProxy action)
    {
        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(action);
    }

}