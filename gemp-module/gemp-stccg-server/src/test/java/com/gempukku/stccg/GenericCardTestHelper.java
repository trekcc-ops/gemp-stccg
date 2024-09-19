package com.gempukku.stccg;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.*;

public class GenericCardTestHelper extends AbstractAtTest {

    private final int LastCardID = 100;

    public static final String FOTRFrodo = "1_290";
    public static final String GimliRB = "9_4";
    public static final String GaladrielRB = "9_14";

    public static final String RulingRing = "1_2";
    public static final String IsildursBaneRing = "1_1";
    public static final String ATARRing = "4_1";
    public static final String GreatRing = "19_1";

    private final CardBlueprintFactory Environment = new CardBlueprintFactory();

    // Player key, then name/card
    public final Map<String, Map<String, PhysicalCardGeneric>> Cards = new HashMap<>();

    public GenericCardTestHelper(HashMap<String, String> cardIDs) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, null, null, null, "multipath");
    }

    public GenericCardTestHelper(HashMap<String, String> cardIDs, HashMap<String, String> siteIDs, String ringBearerID, String ringID, String path) throws CardNotFoundException, DecisionResultInvalidException {
        super();

        initializeSimplestGame();

        Cards.put(P1, new HashMap<>());
        Cards.put(P2, new HashMap<>());

        if(cardIDs != null) {
            for(String name : cardIDs.keySet()) {
                String id = cardIDs.get(name);
                PhysicalCardGeneric card = createCard(P1, id);
                Cards.get(P1).put(name, card);
                FreepsMoveCardsToBottomOfDeck(card);

                card = createCard(P2, id);
                Cards.get(P2).put(name, card);
                ShadowMoveCardsToBottomOfDeck(card);
            }
        }

    }


    public void StartGame() throws DecisionResultInvalidException {
        skipMulligans();
    }

    public void SkipStartingFellowships() throws DecisionResultInvalidException {
        if(FreepsDecisionAvailable("Starting fellowship")) {
            FreepsChoose("");
        }
        if(ShadowDecisionAvailable("Starting fellowship")) {
            ShadowChoose("");
        }
    }

    public PhysicalCardGeneric GetFreepsCard(String cardName) { return Cards.get(P1).get(cardName); }
    public PhysicalCardGeneric GetShadowCard(String cardName) { return Cards.get(P2).get(cardName); }
    public PhysicalCardGeneric GetCard(String player, String cardName) { return Cards.get(player).get(cardName); }
    public PhysicalCardGeneric GetFreepsCardByID(String id) { return GetCardByID(P1, Integer.parseInt(id)); }
    public PhysicalCardGeneric GetFreepsCardByID(int id) { return GetCardByID(P1, id); }
    public PhysicalCardGeneric GetShadowCardByID(String id) { return GetCardByID(P2, Integer.parseInt(id)); }
    public PhysicalCardGeneric GetShadowCardByID(int id) { return GetCardByID(P2, id); }
    public PhysicalCardGeneric GetCardByID(String player, int id) {
        return Cards.get(player).values().stream()
                .filter(x -> x.getCardId() == id)
                .findFirst().orElse(null);
    }

    public List<String> GetAvailableActions(String playerID) {
        AwaitingDecision decision = GetAwaitingDecision(playerID);
        if(decision == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(decision.getDecisionParameters().get("actionText"));
    }

    public AwaitingDecision FreepsGetAwaitingDecision() { return GetAwaitingDecision(P1); }
    public AwaitingDecision ShadowGetAwaitingDecision() { return GetAwaitingDecision(P2); }
    public AwaitingDecision GetAwaitingDecision(String playerID) { return _userFeedback.getAwaitingDecision(playerID); }

    public Boolean FreepsDecisionAvailable(String text) { return DecisionAvailable(P1, text); }
    public Boolean ShadowDecisionAvailable(String text) { return DecisionAvailable(P2, text); }
    public Boolean DecisionAvailable(String playerID, String text)
    {
        AwaitingDecision ad = GetAwaitingDecision(playerID);
        if(ad == null)
            return false;
        String lowerText = text.toLowerCase();
        return ad.getText().toLowerCase().contains(lowerText);
    }

    public Boolean FreepsActionAvailable(String action) { return ActionAvailable(P1, action); }
    public Boolean FreepsActionAvailable(PhysicalCardGeneric card) { return ActionAvailable(P1, "Use " + card.getFullName()); }
    public Boolean FreepsPlayAvailable(PhysicalCardGeneric card) { return ActionAvailable(P1, "Play " + card.getFullName()); }
    public Boolean FreepsTransferAvailable(PhysicalCardGeneric card) { return ActionAvailable(P1, "Transfer " + card.getFullName()); }
    public Boolean ShadowActionAvailable(String action) { return ActionAvailable(P2, action); }
    public Boolean ShadowActionAvailable(PhysicalCardGeneric card) { return ActionAvailable(P2, "Use " + card.getFullName()); }
    public Boolean ShadowPlayAvailable(PhysicalCardGeneric card) { return ActionAvailable(P2, "Play " + card.getFullName()); }
    public Boolean ShadowTransferAvailable(PhysicalCardGeneric card) { return ActionAvailable(P2, "Transfer " + card.getFullName()); }
    public Boolean ActionAvailable(String player, String action) {
        List<String> actions = GetAvailableActions(player);
        if(actions == null)
            return false;
        String lowerAction = action.toLowerCase();
        return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerAction));
    }

    public Boolean FreepsChoiceAvailable(String choice) { return ChoiceAvailable(P1, choice); }
    public Boolean ShadowChoiceAvailable(String choice) { return ChoiceAvailable(P2, choice); }
    public Boolean ChoiceAvailable(String player, String choice) {
        List<String> actions = GetADParamAsList(player, "results");
        if(actions == null)
            return false;
        String lowerChoice = choice.toLowerCase();
        return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerChoice));
    }

    public Boolean FreepsAnyActionsAvailable() { return AnyActionsAvailable(P1); }
    public Boolean ShadowAnyActionsAvailable() { return AnyActionsAvailable(P2); }
    public Boolean AnyActionsAvailable(String player) {
        List<String> actions = GetAvailableActions(player);
        return !actions.isEmpty();
    }

    public Boolean FreepsAnyDecisionsAvailable() { return AnyDecisionsAvailable(P1); }
    public Boolean ShadowAnyDecisionsAvailable() { return AnyDecisionsAvailable(P2); }
    public Boolean AnyDecisionsAvailable(String player) {
        AwaitingDecision ad = GetAwaitingDecision(player);
        return ad != null;
    }

    public List<String> FreepsGetCardChoices() { return GetADParamAsList(P1, "cardId"); }
    public List<String> ShadowGetCardChoices() { return GetADParamAsList(P2, "cardId"); }
    public List<String> FreepsGetBPChoices() { return GetADParamAsList(P1, "blueprintId"); }
    public List<String> ShadowGetBPChoices() { return GetADParamAsList(P2, "blueprintId"); }
    public List<String> FreepsGetMultipleChoices() { return GetADParamAsList(P1, "results"); }
    public List<String> ShadowGetMultipleChoices() { return GetADParamAsList(P2, "results"); }
    public List<String> FreepsGetADParamAsList(String paramName) { return GetADParamAsList(P1, paramName); }
    public List<String> ShadowGetADParamAsList(String paramName) { return GetADParamAsList(P2, paramName); }
    public List<String> GetADParamAsList(String playerID, String paramName) { return Arrays.asList(GetAwaitingDecisionParam(playerID, paramName)); }
    public String[] FreepsGetADParam(String paramName) { return GetAwaitingDecisionParam(P1, paramName); }
    public String[] ShadowGetADParam(String paramName) { return GetAwaitingDecisionParam(P2, paramName); }
    public String FreepsGetFirstADParam(String paramName) { return GetAwaitingDecisionParam(P1, paramName)[0]; }
    public String ShadowGetFirstADParam(String paramName) { return GetAwaitingDecisionParam(P2, paramName)[0]; }
    public String[] GetAwaitingDecisionParam(String playerID, String paramName) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters().get(paramName);
    }

    public Map<String, String[]> GetAwaitingDecisionParams(String playerID) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters();
    }

    //public boolean HasItemIn

    public void FreepsUseCardAction(String name) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, name)); }
    public void FreepsUseCardAction(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, "Use " + card.getFullName())); }
    public void FreepsTransferCard(String name) throws DecisionResultInvalidException { FreepsTransferCard(GetFreepsCard(name)); }
    public void FreepsTransferCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, "Transfer " + card.getFullName())); }
    public void ShadowUseCardAction(String name) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, name)); }
    public void ShadowUseCardAction(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, "Use " + card.getFullName())); }
    public void ShadowTransferCard(String name) throws DecisionResultInvalidException { ShadowTransferCard(GetShadowCard(name)); }
    public void ShadowTransferCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, "Transfer " + card.getFullName())); }

    public void FreepsPlayCard(String name) throws DecisionResultInvalidException { FreepsPlayCard(GetFreepsCard(name)); }
    public void FreepsPlayCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, "Play " + card.getFullName())); }
    public void ShadowPlayCard(String name) throws DecisionResultInvalidException { ShadowPlayCard(GetShadowCard(name)); }
    public void ShadowPlayCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, "Play " + card.getFullName())); }

    public List<? extends PhysicalCard> GetFreepsHand() { return GetPlayerHand(P1); }
    public List<? extends PhysicalCard> GetShadowHand() { return GetPlayerHand(P2); }
    public List<? extends PhysicalCard> GetPlayerHand(String player)
    {
        return _game.getGameState().getHand(player);
    }

    public int GetFreepsDeckCount() { return GetPlayerDeckCount(P1); }
    public int GetShadowDeckCount() { return GetPlayerDeckCount(P2); }
    public int GetPlayerDeckCount(String player)
    {
        return _game.getGameState().getDrawDeck(player).size();
    }

    public PhysicalCardGeneric GetFreepsBottomOfDeck() { return GetPlayerBottomOfDeck(P1); }
    public PhysicalCardGeneric GetShadowBottomOfDeck() { return GetPlayerBottomOfDeck(P2); }
    public PhysicalCardGeneric GetFromBottomOfFreepsDeck(int index) { return GetFromBottomOfPlayerDeck(P1, index); }
    public PhysicalCardGeneric GetFromBottomOfShadowDeck(int index) { return GetFromBottomOfPlayerDeck(P2, index); }
    public PhysicalCardGeneric GetPlayerBottomOfDeck(String player) { return GetFromBottomOfPlayerDeck(player, 1); }
    public PhysicalCardGeneric GetFromBottomOfPlayerDeck(String player, int index)
    {
        var deck = _game.getGameState().getDrawDeck(player);
        return (PhysicalCardGeneric) deck.get(deck.size() - index);
    }

    public PhysicalCardGeneric GetFreepsTopOfDeck() { return GetPlayerTopOfDeck(P1); }
    public PhysicalCardGeneric GetShadowTopOfDeck() { return GetPlayerTopOfDeck(P2); }
    public PhysicalCardGeneric GetFromTopOfFreepsDeck(int index) { return GetFromTopOfPlayerDeck(P1, index); }
    public PhysicalCardGeneric GetFromTopOfShadowDeck(int index) { return GetFromTopOfPlayerDeck(P2, index); }
    public PhysicalCardGeneric GetPlayerTopOfDeck(String player) { return GetFromTopOfPlayerDeck(player, 1); }

    /**
     * Index is 1-based (1 is first, 2 is second, etc.)
     */
    public PhysicalCardGeneric GetFromTopOfPlayerDeck(String player, int index)
    {
        var deck = _game.getGameState().getDrawDeck(player);
        return (PhysicalCardGeneric) deck.get(index - 1);
    }

    public Phase GetCurrentPhase() { return _game.getGameState().getCurrentPhase(); }



    public void FreepsMoveCardToHand(String...names) {
        for(String name : names) {
            FreepsMoveCardToHand(GetFreepsCard(name));
        }
    }
    public void FreepsMoveCardToHand(PhysicalCardGeneric...cards) {
        for(PhysicalCardGeneric card : cards) {
            RemoveCardZone(P1, card);
            MoveCardToZone(P1, card, Zone.HAND);
        }
    }
    public void ShadowMoveCardToHand(String...names) {
        for(String name : names) {
            ShadowMoveCardToHand(GetShadowCard(name));
        }
    }
    public void ShadowMoveCardToHand(PhysicalCardGeneric...cards) {
        for(PhysicalCardGeneric card : cards) {
            RemoveCardZone(P2, card);
            MoveCardToZone(P2, card, Zone.HAND);
        }
    }

    public void FreepsAttachCardsTo(PhysicalCardGeneric bearer, PhysicalCardGeneric...cards) { AttachCardsTo(bearer, cards); }
    public void FreepsAttachCardsTo(PhysicalCardGeneric bearer, String...names) {
        Arrays.stream(names).forEach(name -> AttachCardsTo(bearer, GetFreepsCard(name)));
    }
    public void ShadowAttachCardsTo(PhysicalCardGeneric bearer, PhysicalCardGeneric...cards) { AttachCardsTo(bearer, cards); }
    public void ShadowAttachCardsTo(PhysicalCardGeneric bearer, String...names) {
        Arrays.stream(names).forEach(name -> AttachCardsTo(bearer, GetShadowCard(name)));
    }
    public void AttachCardsTo(PhysicalCardGeneric bearer, PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().attachCard(card, bearer);
        });
    }

    public void FreepsStackCardsOn(PhysicalCardGeneric on, String...cardNames) {
        Arrays.stream(cardNames).forEach(name -> StackCardsOn(on, GetFreepsCard(name)));
    }
    public void ShadowStackCardsOn(PhysicalCardGeneric on, String...cardNames) {
        Arrays.stream(cardNames).forEach(name -> StackCardsOn(on, GetShadowCard(name)));
    }
    public void StackCardsOn(PhysicalCardGeneric on, PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().stackCard(card, on);
        });
    }

    public void FreepsMoveCardsToTopOfDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> FreepsMoveCardsToTopOfDeck(GetFreepsCard(cardName)));
    }
    public void FreepsMoveCardsToTopOfDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnTopOfDeck(card);
        });
    }
    public void ShadowMoveCardsToTopOfDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> ShadowMoveCardsToTopOfDeck(GetShadowCard(cardName)));
    }
    public void ShadowMoveCardsToTopOfDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnTopOfDeck(card);
        });
    }

    public void FreepsMoveCardsToBottomOfDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> FreepsMoveCardsToBottomOfDeck(GetFreepsCard(cardName)));
    }
    public void FreepsMoveCardsToBottomOfDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnBottomOfDeck(card);
        });
    }
    public void ShadowMoveCardsToBottomOfDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> ShadowMoveCardsToBottomOfDeck(GetShadowCard(cardName)));
    }
    public void ShadowMoveCardsToBottomOfDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnBottomOfDeck(card);
        });
    }

    public void FreepsDrawCard() { FreepsDrawCards(1); }
    public void FreepsDrawCards() { FreepsDrawCards(1); }

    public void FreepsDrawCards(int count) {
        for(int i = 0; i < count; i++) {
            _game.getGameState().playerDrawsCard(P1);
        }
    }

    public void ShadowDrawCard() { ShadowDrawCards(1); }
    public void ShadowDrawCards() { ShadowDrawCards(1); }

    public void ShadowDrawCards(int count) {
        for(int i = 0; i < count; i++) {
            _game.getGameState().playerDrawsCard(P2);
        }
    }

    public void FreepsShuffleCardsInDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> FreepsShuffleCardsInDeck(GetFreepsCard(cardName)));
    }
    public void FreepsShuffleCardsInDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnTopOfDeck(card);
        });

        ShuffleFreepsDeck();
    }
    public void ShadowShuffleCardsInDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> ShadowShuffleCardsInDeck(GetShadowCard(cardName)));
    }
    public void ShadowShuffleCardsInDeck(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> {
            RemoveCardZone(card.getOwnerName(), card);
            _game.getGameState().putCardOnTopOfDeck(card);
        });

        ShuffleShadowDeck();
    }

    public void ShuffleFreepsDeck() { ShuffleDeck(P1); }
    public void ShuffleShadowDeck() { ShuffleDeck(P2); }
    public void ShuffleDeck(String player) {
        _game.getGameState().shuffleDeck(player);
    }


    public void FreepsMoveCardToSupportArea(String...names) {
        Arrays.stream(names).forEach(name -> FreepsMoveCardToSupportArea(GetFreepsCard(name)));
    }
    public void FreepsMoveCardToSupportArea(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.SUPPORT));
    }
    public void ShadowMoveCardToSupportArea(String...names) {
        Arrays.stream(names).forEach(name -> ShadowMoveCardToSupportArea(GetShadowCard(name)));
    }
    public void ShadowMoveCardToSupportArea(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P2, card, Zone.SUPPORT));
    }
    public void FreepsMoveCardToDiscard(String...names) {
        Arrays.stream(names).forEach(name -> FreepsMoveCardToDiscard(GetFreepsCard(name)));
    }
    public void FreepsMoveCardToDiscard(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.DISCARD));
    }
    public void ShadowMoveCardToDiscard(String...names) {
        Arrays.stream(names).forEach(name -> ShadowMoveCardToDiscard(GetShadowCard(name)));
    }
    public void ShadowMoveCardToDiscard(PhysicalCardGeneric...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P2, card, Zone.DISCARD));
    }


    public void RemoveCardZone(String player, PhysicalCardGeneric card) {
        if(card.getZone() != null)
        {
            _game.getGameState().removeCardsFromZone(player, new ArrayList<>() {{
                add(card);
            }});
        }
    }

    public void MoveCardToZone(String player, PhysicalCardGeneric card, Zone zone) {
        RemoveCardZone(player, card);
        _game.getGameState().addCardToZone(card, zone);
    }


    public int GetTwilight() { return _game.getGameState().getTwilightPool(); }

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
        FreepsPassCurrentPhaseAction();
        ShadowPassCurrentPhaseAction();
    }

    public void FreepsPassCurrentPhaseAction() throws DecisionResultInvalidException {
        if(_userFeedback.getAwaitingDecision(P1) != null) {
            playerDecided(P1, "");
        }
    }

    public void ShadowPassCurrentPhaseAction() throws DecisionResultInvalidException {
        if(_userFeedback.getAwaitingDecision(P2) != null) {
            playerDecided(P2, "");
        }
    }

    public void FreepsDismissRevealedCards() throws DecisionResultInvalidException { FreepsPassCurrentPhaseAction(); }
    public void ShadowDismissRevealedCards() throws DecisionResultInvalidException { ShadowPassCurrentPhaseAction(); }
    public void DismissRevealedCards() throws DecisionResultInvalidException {
        FreepsDismissRevealedCards();
        ShadowDismissRevealedCards();
    }

    public void FreepsDeclineAssignments() throws DecisionResultInvalidException { FreepsPassCurrentPhaseAction(); }
    public void ShadowDeclineAssignments() throws DecisionResultInvalidException { ShadowPassCurrentPhaseAction(); }


    public List<PhysicalCardGeneric> FreepsGetAttachedCards(String name) { return GetAttachedCards(GetFreepsCard(name)); }
    public List<PhysicalCardGeneric> ShadowGetAttachedCards(String name) { return GetAttachedCards(GetShadowCard(name)); }
    public List<PhysicalCardGeneric> GetAttachedCards(PhysicalCardGeneric card) {
        return (List<PhysicalCardGeneric>)(List<?>)_game.getGameState().getAttachedCards(card);
    }

    public List<PhysicalCardGeneric> FreepsGetStackedCards(String name) { return GetStackedCards(GetFreepsCard(name)); }
    public List<PhysicalCardGeneric> ShadowGetStackedCards(String name) { return GetStackedCards(GetShadowCard(name)); }
    public List<PhysicalCardGeneric> GetStackedCards(PhysicalCardGeneric card) {
        return (List<PhysicalCardGeneric>)(List<?>)card.getStackedCards();
    }

    public void FreepsResolveSkirmish(String name) throws DecisionResultInvalidException { FreepsResolveSkirmish(GetFreepsCard(name)); }
    public void FreepsResolveSkirmish(PhysicalCardGeneric comp) throws DecisionResultInvalidException { FreepsChooseCard(comp); }

    public void FreepsChooseCard(String name) throws DecisionResultInvalidException { FreepsChooseCard(GetFreepsCard(name)); }
    public void FreepsChooseCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P1, String.valueOf(card.getCardId())); }
    public void ShadowChooseCard(String name) throws DecisionResultInvalidException { ShadowChooseCard(GetShadowCard(name)); }
    public void ShadowChooseCard(PhysicalCardGeneric card) throws DecisionResultInvalidException { playerDecided(P2, String.valueOf(card.getCardId())); }

    public void FreepsChooseAnyCard() throws DecisionResultInvalidException { FreepsChoose(FreepsGetCardChoices().get(0)); }
    public void ShadowChooseAnyCard() throws DecisionResultInvalidException { ShadowChoose(ShadowGetCardChoices().get(0)); }

    public void FreepsChooseCards(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCards(P1, cards); }
    public void ShadowChooseCards(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCards(P2, cards); }
    public void ChooseCards(String player, PhysicalCardGeneric...cards) throws DecisionResultInvalidException {
        String[] ids = new String[cards.length];

        for(int i = 0; i < cards.length; i++)
        {
            ids[i] = String.valueOf(cards[i].getCardId());
        }

        playerDecided(player, String.join(",", ids));
    }



    public boolean FreepsCanChooseCharacter(PhysicalCardGeneric card) { return FreepsGetCardChoices().contains(String.valueOf(card.getCardId())); }
    public boolean ShadowCanChooseCharacter(PhysicalCardGeneric card) { return ShadowGetCardChoices().contains(String.valueOf(card.getCardId())); }

    public int GetFreepsCardChoiceCount() { return FreepsGetCardChoices().size(); }
    public int GetShadowCardChoiceCount() { return ShadowGetCardChoices().size(); }

    public void FreepsChooseCardBPFromSelection(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCardBPFromSelection(P1, cards);}
    public void ShadowChooseCardBPFromSelection(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCardBPFromSelection(P2, cards);}

    public void ChooseCardBPFromSelection(String player, PhysicalCardGeneric...cards) throws DecisionResultInvalidException {
        String[] choices = GetAwaitingDecisionParam(player,"blueprintId");
        ArrayList<String> bps = new ArrayList<>();
        ArrayList<PhysicalCardGeneric> found = new ArrayList<>();

        for(int i = 0; i < choices.length; i++)
        {
            for(PhysicalCardGeneric card : cards)
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

    public void FreepsChooseCardIDFromSelection(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCardIDFromSelection(P1, cards);}
    public void ShadowChooseCardIDFromSelection(PhysicalCardGeneric...cards) throws DecisionResultInvalidException { ChooseCardIDFromSelection(P2, cards);}

    public void ChooseCardIDFromSelection(String player, PhysicalCardGeneric...cards) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        //playerDecided(player, "" + card.getCardId());

        String[] choices = GetAwaitingDecisionParam(player,"cardId");
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<PhysicalCardGeneric> found = new ArrayList<>();

        for (String choice : choices) {
            for (PhysicalCardGeneric card : cards) {
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

    public boolean IsAttachedTo(PhysicalCardGeneric card, PhysicalCardGeneric bearer) {
        if(card.getZone() != Zone.ATTACHED) {
            return false;
        }

        return bearer == card.getAttachedTo();
    }


    public int GetStrength(PhysicalCardGeneric card)
    {
        return _game.getModifiersQuerying().getStrength(card);
    }

    public boolean HasKeyword(PhysicalCardGeneric card, Keyword keyword)
    {
        return _game.getModifiersQuerying().hasKeyword(card, keyword);
    }

    public int GetKeywordCount(PhysicalCardGeneric card, Keyword keyword)
    {
        return _game.getModifiersQuerying().getKeywordCount(card, keyword);
    }

    public boolean IsType(PhysicalCardGeneric card, CardType type)
    {
        return card.getCardType() == type
            || _game.getModifiersQuerying().isAdditionalCardType(_game, card, type);
    }


    public void ApplyAdHocModifier(Modifier mod)
    {
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(mod);
    }

    public void ApplyAdHocAction(ActionProxy action)
    {
        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(action);
    }

    public void FreepsChoose(String choice) throws DecisionResultInvalidException { playerDecided(P1, choice); }
    public void FreepsChoose(String...choices) throws DecisionResultInvalidException { playerDecided(P1, String.join(",", choices)); }
    public void ShadowChoose(String choice) throws DecisionResultInvalidException { playerDecided(P2, choice); }
    public void ShadowChoose(String...choices) throws DecisionResultInvalidException { playerDecided(P2, String.join(",", choices)); }


    public void FreepsChooseToStay() throws DecisionResultInvalidException { playerDecided(P1, "1"); }
    public void ShadowChooseToStay() throws DecisionResultInvalidException { playerDecided(P2, "1"); }

    public boolean FreepsHasOptionalTriggerAvailable() { return FreepsDecisionAvailable("Optional"); }
    public boolean ShadowHasOptionalTriggerAvailable() { return ShadowDecisionAvailable("Optional"); }

    public void FreepsAcceptOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P1, "0"); }
    public void FreepsDeclineOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P1, ""); }
    public void ShadowAcceptOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P2, "0"); }
    public void ShadowDeclineOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P2, ""); }

    public void FreepsDeclineReconciliation() throws DecisionResultInvalidException { FreepsPassCurrentPhaseAction(); }

    public void FreepsChooseYes() throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P1, "Yes"); }
    public void ShadowChooseYes() throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P2, "Yes"); }
    public void FreepsChooseNo() throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P1, "No"); }
    public void ShadowChooseNo() throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P2, "No"); }
    public void FreepsChooseMultipleChoiceOption(String option) throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P1, option); }
    public void ShadowChooseMultipleChoiceOption(String option) throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P2, option); }
    public void ChooseMultipleChoiceOption(String playerID, String option) throws DecisionResultInvalidException { ChooseAction(playerID, "results", option); }
    public void FreepsChooseAction(String paramName, String option) throws DecisionResultInvalidException { ChooseAction(P1, paramName, option); }
    public void FreepsChooseAction(String option) throws DecisionResultInvalidException { ChooseAction(P1, "actionText", option); }
    public void ShadowChooseAction(String paramName, String option) throws DecisionResultInvalidException { ChooseAction(P2, paramName, option); }
    public void ShadowChooseAction(String option) throws DecisionResultInvalidException { ChooseAction(P2, "actionText", option); }
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

    public void FreepsResolveActionOrder(String option) throws DecisionResultInvalidException { ChooseAction(P1, "actionText", option); }

    public void AcknowledgeReveal() throws DecisionResultInvalidException
    {
        playerDecided(P1, "");
        playerDecided(P2, "");
    }

}
