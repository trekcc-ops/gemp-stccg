package com.gempukku.stccg;

import com.gempukku.stccg.actions.ActionProxy;
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

    public List<? extends PhysicalCard> GetPlayerHand(String player)
    {
        return _game.getGameState().getHand(player);
    }

    public int GetPlayerDeckCount(String player)
    {
        return _game.getGameState().getDrawDeck(player).size();
    }

    public PhysicalCard GetPlayerBottomOfDeck(String player) { return GetFromBottomOfPlayerDeck(player, 1); }
    public PhysicalCard GetFromBottomOfPlayerDeck(String player, int index)
    {
        var deck = _game.getGameState().getDrawDeck(player);
        return deck.get(deck.size() - index);
    }

    public PhysicalCard GetPlayerTopOfDeck(String player) { return GetFromTopOfPlayerDeck(player, 1); }

    /**
     * Index is 1-based (1 is first, 2 is second, etc.)
     */
    public PhysicalCard GetFromTopOfPlayerDeck(String player, int index)
    {
        var deck = _game.getGameState().getDrawDeck(player);
        return deck.get(index - 1);
    }

    public Phase GetCurrentPhase() { return _game.getGameState().getCurrentPhase(); }



    public void ShuffleDeck(String player) {
        _game.getGameState().shuffleDeck(player);
    }


    public void SkipToPhase(Phase target) throws DecisionResultInvalidException {
        for(int attempts = 1; attempts <= 20; attempts++)
        {
            Phase current = _game.getGameState().getCurrentPhase();
            if(current == target)
                break;

            PassCurrentPhaseActions();

            if(attempts == 20)
            {
                throw new DecisionResultInvalidException("Could not arrive at target '" + target + "' after 20 attempts!");
            }
        }
    }

    public void SkipToPhaseInverted(Phase target) throws DecisionResultInvalidException {
        for(int attempts = 1; attempts <= 20; attempts++)
        {
            Phase current = _game.getGameState().getCurrentPhase();
            if(current == target)
                break;

            PassCurrentPhaseActions();

            if(attempts == 20)
            {
                throw new DecisionResultInvalidException("Could not arrive at target '" + target + "' after 20 attempts!");
            }
        }
    }

    public void PassCurrentPhaseActions() throws DecisionResultInvalidException {
        if(_userFeedback.getAwaitingDecision(P1) != null) {
            playerDecided(P1, "");
        }
        if(_userFeedback.getAwaitingDecision(P2) != null) {
            playerDecided(P2, "");
        }
    }

    public void ChooseCards(String player, PhysicalCard...cards) throws DecisionResultInvalidException {
        String[] ids = new String[cards.length];

        for(int i = 0; i < cards.length; i++)
        {
            ids[i] = String.valueOf(cards[i].getCardId());
        }

        playerDecided(player, String.join(",", ids));
    }



    public void ChooseCardBPFromSelection(String player, PhysicalCard...cards) throws DecisionResultInvalidException {
        String[] choices = GetAwaitingDecisionParam(player,"blueprintId");
        Collection<String> bps = new ArrayList<>();
        Collection<PhysicalCard> found = new ArrayList<>();

        for(int i = 0; i < choices.length; i++)
        {
            for(PhysicalCard card : cards)
            {
                if(found.contains(card))
                    continue;

                if(Objects.equals(card.getBlueprintId(), choices[i]))
                {
                    // I have no idea why the spacing is required, but the BP parser skips to the fourth position
                    bps.add("    " + i);
                    found.add(card);
                    break;
                }
            }
        }

        playerDecided(player, String.join(",", bps));
    }

    public void ChooseCardIDFromSelection(String player, PhysicalCard...cards) throws DecisionResultInvalidException {
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


    public int GetStrength(PhysicalCard card)
    {
        return _game.getModifiersQuerying().getStrength(card);
    }


    public void ApplyAdHocModifier(Modifier mod)
    {
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(mod);
    }

    public void ApplyAdHocAction(ActionProxy action)
    {
        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(action);
    }

    public void ChooseMultipleChoiceOption(String playerID, String option) throws DecisionResultInvalidException { ChooseAction(playerID, "results", option); }
    public void ChooseAction(String playerID, String paramName, String option) throws DecisionResultInvalidException {
        List<String> choices = GetADParamAsList(playerID, paramName);
        for(String choice : choices){
            if(choice.toLowerCase().contains(option.toLowerCase())) {
                playerDecided(playerID, String.valueOf(choices.indexOf(choice)));
                return;
            }
        }
        //couldn't find an exact match, so maybe it's a direct index:
        playerDecided(playerID, option);
    }

    public void AcknowledgeReveal() throws DecisionResultInvalidException
    {
        playerDecided(P1, "");
        playerDecided(P2, "");
    }

}