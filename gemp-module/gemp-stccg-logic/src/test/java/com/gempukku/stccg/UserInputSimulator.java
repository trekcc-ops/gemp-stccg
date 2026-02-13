package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.battle.InitiateShipBattleAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.placecard.AddCardsToSeedCardStackAction;
import com.gempukku.stccg.actions.placecard.RemoveCardsFromSeedCardStackAction;
import com.gempukku.stccg.actions.playcard.*;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public interface UserInputSimulator {

    // For methods that will be used in testing to simulate user responses.

    DefaultGame getGame();

    default void performAction(String playerId, Class<? extends Action> actionClass, PhysicalCard performingCard)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        Action choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (TopLevelSelectableAction action : actionDecision.getActions()) {
                if (action.getPerformingCard() == performingCard &&
                        actionClass.isAssignableFrom(action.getClass()))
                    choice = action;
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null) {
            throw new DecisionResultInvalidException("Could not find game text action");
        }
    }


    private void selectAction(String playerId, Action action)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            actionDecision.decisionMade(action);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
    }

    default <T extends Action> void skipAction(String playerId, Class<T> clazz)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<Action> selectableActions = getSelectableActionsOfClass(playerId, Action.class);
        if (selectableActions.size() == 1) {
            Action selectableAction = Iterables.getOnlyElement(selectableActions);
            if (clazz.isAssignableFrom(selectableAction.getClass())) {
                playerDecided(playerId, "");
            }
        } else {
            throw new DecisionResultInvalidException("Too many actions available. Cannot skip.");
        }
    }

    
    default InitiateShipBattleAction initiateBattle(String initiatingPlayerName)
            throws InvalidGameOperationException, DecisionResultInvalidException {
        List<InitiateShipBattleAction> selectableActions =
                getSelectableActionsOfClass(initiatingPlayerName, InitiateShipBattleAction.class);
        for (InitiateShipBattleAction action : selectableActions) {
            selectAction(initiatingPlayerName, action);
            return action;
        }
        throw new DecisionResultInvalidException("Could not initiate battle");
    }

    default <T extends Action> T selectAction(Class<T> clazz, PhysicalCard card, String playerId)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        T choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (TopLevelSelectableAction action : actionDecision.getActions()) {
                if (clazz.isAssignableFrom(action.getClass())) {
                    if (action.getPerformingCard() == card) {
                        choice = (T) action;
                    }
                } else if (action instanceof UseGameTextAction useTextAction) {
                    if (useTextAction.getPerformingCard() == card &&
                            clazz.isAssignableFrom(useTextAction.getSubActions().getFirst().getClass())) {
                        choice = (T) action;
                    }
                }
            }
            if (choice != null) {
                actionDecision.decisionMade(choice);
                getGame().removeDecision(playerId);
                getGame().carryOutPendingActionsUntilDecisionNeeded();
            }
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action found");
        else return choice;
    }

    default void selectCard(String playerId, PhysicalCard card) 
            throws DecisionResultInvalidException, InvalidGameOperationException {
        selectCards(playerId, List.of(card));
    }

    default List<? extends PhysicalCard> getSelectableCards(String playerId) throws DecisionResultInvalidException {
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof CardSelectionDecision cardSelection) {
            return cardSelection.getSelectableCards();
        } else {
            throw new DecisionResultInvalidException("No current decision allows selecting of cards");
        }
    }

    default void selectCards(String playerId, List<PhysicalCard> cards)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof CardSelectionDecision cardSelection) {
            cardSelection.decisionMade(cards);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        } else {
            throw new DecisionResultInvalidException("No current decision allows selecting of cards");
        }
    }

    default void useGameText(PhysicalCard card, String playerId) 
            throws DecisionResultInvalidException, InvalidGameOperationException {
        Action choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (TopLevelSelectableAction action : actionDecision.getActions()) {
                if (action.getPerformingCard() == card)
                    choice = action;
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null) {
            throw new DecisionResultInvalidException("Could not find game text action");
        }
    }

    default <T extends Action> List<T> getSelectableActionsOfClass(String playerName, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        AwaitingDecision decision = getGame().getAwaitingDecision(playerName);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (clazz.isAssignableFrom(action.getClass())) {
                    result.add((T) action);
                }
            }
        }
        return result;
    }

    default Action seedCard(String playerId, PhysicalCard cardToSeed)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<SeedCardAction> selectableActions = getSelectableActionsOfClass(playerId, SeedCardAction.class);
        for (SeedCardAction action : selectableActions) {
            if (action.getCardEnteringPlay() == cardToSeed) {
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    default void chooseOnlyAction(String playerId)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        Action choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            if (actionDecision.getActions().size() == 1) {
                choice = actionDecision.getActions().getFirst();
                actionDecision.decisionMade(choice);
                getGame().removeDecision(playerId);
                getGame().carryOutPendingActionsUntilDecisionNeeded();
            }
        }
        if (choice == null)
            throw new DecisionResultInvalidException("Could not choose a valid action");
    }


    default AttemptMissionAction attemptMission(String playerId, MissionCard mission)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<AttemptMissionAction> selectableActions =
                getSelectableActionsOfClass(playerId, AttemptMissionAction.class);
        for (AttemptMissionAction action : selectableActions) {
            if (action.getLocationId() == mission.getLocationId()) {
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException("No valid action to attempt " + mission.getTitle());
    }


    default void seedMission(MissionCard mission) throws DecisionResultInvalidException,
            InvalidGameOperationException {
        String playerName = mission.getOwnerName();
        AwaitingDecision missionSelection = getGame().getAwaitingDecision(playerName);
        if (missionSelection instanceof ActionSelectionDecision actionDecision) {
            String actionId = null;
            for (int i = 0; i < actionDecision.getActions().size(); i++) {
                if (actionDecision.getActions().get(i) instanceof SeedMissionCardAction seedAction &&
                        seedAction.getCardEnteringPlay() == mission) {
                    actionId = String.valueOf(seedAction.getActionId());
                }
            }
            playerDecided(playerName, actionId);
        } else {
            throw new DecisionResultInvalidException("Could not find action selection decision");
        }
    }


    default void seedDilemma(PhysicalCard seedCard, MissionLocation mission) throws DecisionResultInvalidException,
            InvalidGameOperationException {
        String playerName = seedCard.getOwnerName();
        AwaitingDecision missionSelection = getGame().getAwaitingDecision(playerName);
        if (missionSelection instanceof ActionSelectionDecision actionDecision) {
            String actionId = null;
            for (int i = 0; i < actionDecision.getActions().size(); i++) {
                if (actionDecision.getActions().get(i) instanceof AddCardsToSeedCardStackAction seedAction &&
                        seedAction.getLocationId() == mission.getLocationId()) {
                    actionId = String.valueOf(seedAction.getActionId());
                }
            }
            playerDecided(playerName, actionId);

            playerDecided(playerName, String.valueOf(seedCard.getCardId()));
        } else {
            throw new DecisionResultInvalidException("Could not find action selection decision");
        }
    }

    default void removeDilemma(PhysicalCard seedCard, MissionLocation mission) throws DecisionResultInvalidException,
            InvalidGameOperationException {
        String playerName = seedCard.getOwnerName();
        AwaitingDecision missionSelection = getGame().getAwaitingDecision(playerName);
        if (missionSelection instanceof ActionSelectionDecision actionDecision) {
            String actionId = null;
            for (int i = 0; i < actionDecision.getActions().size(); i++) {
                if (actionDecision.getActions().get(i) instanceof RemoveCardsFromSeedCardStackAction seedAction &&
                        seedAction.getLocation() == mission) {
                    actionId = String.valueOf(seedAction.getActionId());
                }
            }
            playerDecided(playerName, actionId);

            if (getGame().getAwaitingDecision(playerName) instanceof ArbitraryCardsSelectionDecision dilemmaSelection)
                playerDecided(playerName, dilemmaSelection.getCardIdForCard(seedCard));
            else throw new InvalidGameLogicException("Player decision is not the expected type");
        } else throw new DecisionResultInvalidException("Unable to find action selection decision");
    }


    default void autoSeedMissions() throws DecisionResultInvalidException, InvalidGameOperationException {
        // Both players keep picking option #1 until all missions are seeded
        while (getGame().getGameState().getCurrentPhase() == Phase.SEED_MISSION) {
            boolean playerDecided = false;
            for (String playerName : getGame().getAllPlayerIds()) {
                if (getGame().getAwaitingDecision(playerName) != null && !playerDecided) {
                    if (ActionSelectionDecision.class.isAssignableFrom(getGame().getAwaitingDecision(playerName).getClass())) {
                        playerDecided(playerName, String.valueOf(((ActionSelectionDecision) getGame().getAwaitingDecision(playerName)).getActions().getFirst().getActionId()));
                        playerDecided = true;
                    } else {
                        playerDecided(playerName, "0");
                        playerDecided = true;
                    }
                }
            }
        }
    }

    default void seedFacility(String playerId, PhysicalCard cardToSeed)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        SeedFacilityAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedFacilityAction seedAction && seedAction.getCardEnteringPlay() == cardToSeed) {
                    choice = seedAction;
                }
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    default void seedFacility(String playerId, PhysicalCard cardToSeed, MissionCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        SeedFacilityAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedFacilityAction seedAction && seedAction.getCardEnteringPlay() == cardToSeed) {
                    choice = seedAction;
                }
            }
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    default Action playCard(String playerId, PhysicalCard cardToPlay)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<PlayCardAction> actions = getSelectableActionsOfClass(playerId, PlayCardAction.class);
        for (PlayCardAction action : actions) {
            if (action.getSelectableCardsToPlay(getGame()).contains(cardToPlay)) {
                if (action instanceof SelectAndReportForFreeCardAction reportAction) {
                    reportAction.setCardReporting(cardToPlay);
                }
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException("No valid action to play " + cardToPlay.getTitle());
    }

    default DownloadAction initiateDownloadAction(String playerId, PhysicalCard performingCard)
            throws InvalidGameOperationException, DecisionResultInvalidException {
        List<DownloadAction> actions = getSelectableActionsOfClass(playerId, DownloadAction.class);
        for (DownloadAction action : actions) {
            if (action.getPerformingCard() == performingCard) {
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException("Could not identify a valid download action");
    }

    default DownloadAction downloadCard(String playerId, PhysicalCard cardToPlay)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<DownloadAction> actions = getSelectableActionsOfClass(playerId, DownloadAction.class);
        for (DownloadAction action : actions) {
            if (action.getDownloadableTargets(getGame()).contains(cardToPlay)) {
                action.setCardToDownload(cardToPlay);
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException("No valid action to download " + cardToPlay.getTitle());
    }


    default Action beamCards(String playerId, PhysicalCard cardWithTransporters,
                             Collection<? extends ReportableCard> cardsToBeam, PhysicalCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        List<BeamCardsAction> actions = getSelectableActionsOfClass(playerId, BeamCardsAction.class);
        for (BeamCardsAction action : actions) {
            if (action.getCardUsingTransporters() == cardWithTransporters) {
                action.setOrigin(cardWithTransporters);
                action.setCardsToMove(cardsToBeam);
                action.setDestination(destination);
                selectAction(playerId, action);
                return action;
            }
        }
        throw new DecisionResultInvalidException(
                "No valid action to beam " + TextUtils.getConcatenatedCardLinks(cardsToBeam));
    }

    default Action beamCard(String playerId, PhysicalCard cardWithTransporters, ReportableCard cardToBeam,
                            PhysicalCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        return beamCards(playerId, cardWithTransporters, List.of(cardToBeam), destination);
    }

    default void playerDecided(String player, String answer) throws DecisionResultInvalidException,
            InvalidGameOperationException {
        AwaitingDecision decision = getGame().getAwaitingDecision(player);
        getGame().removeDecision(player);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            getGame().sendAwaitingDecision(decision);
            throw exp;
        }
        getGame().carryOutPendingActionsUntilDecisionNeeded();
    }

    private void skipSeedPhase() throws InvalidGameOperationException, DecisionResultInvalidException {
        skipDilemma();
        skipFacility();
    }

    default void skipToNextTurnAndPhase(String turnPlayerName, Phase phase)
            throws InvalidGameOperationException, DecisionResultInvalidException {

        GameState gameState = getGame().getGameState();
        int initialTurnNumber = gameState.getCurrentTurnNumber();
        boolean initiallySeeding = getGame().getCurrentPhase().isSeedPhase();

        if (initiallySeeding) {
            skipSeedPhase();
        }

        assertFalse(getGame().getCurrentPhase().isSeedPhase());
        int turnNumberAfterSeeding = gameState.getCurrentTurnNumber();
        String initialPlayerId = getGame().getCurrentPlayerId();
        assertTrue(turnNumberAfterSeeding == initialTurnNumber || turnNumberAfterSeeding == 1);

        if (!initiallySeeding || !initialPlayerId.equals(turnPlayerName)) {
            do {
                int turnNumberBeforeLoop = gameState.getCurrentTurnNumber();
                skipPhase(Phase.START_OF_TURN);
                skipPhase(Phase.CARD_PLAY);
                skipPhase(Phase.EXECUTE_ORDERS);
                skipPhase(Phase.END_OF_TURN);
                assertEquals(turnNumberBeforeLoop + 1, gameState.getCurrentTurnNumber());
            } while (!getGame().getCurrentPlayerId().equals(turnPlayerName));
        }

        int turnNumberAfterSkippingTurns = gameState.getCurrentTurnNumber();
        int expectedTurnsSkipped = 0;
        if (initiallySeeding) {
            expectedTurnsSkipped = (initialPlayerId.equals(turnPlayerName)) ? 1 : 2;
        } else {
            expectedTurnsSkipped = (initialPlayerId.equals(turnPlayerName)) ? 2 : 1;
        }

        assertEquals(turnNumberAfterSkippingTurns, initialTurnNumber + expectedTurnsSkipped);

        if (phase == Phase.EXECUTE_ORDERS) {
            skipPhase(Phase.START_OF_TURN);
            skipPhase(Phase.CARD_PLAY);
        }
    }


    default void skipPhase(Phase phase) throws DecisionResultInvalidException, InvalidGameOperationException {
        String currentPlayerId = getGame().getCurrentPlayerId();
        int initialTurnNumber = getGame().getGameState().getCurrentTurnNumber();
        String selectingPlayerId = currentPlayerId;
        while (getGame().getCurrentPhase() == phase && getGame().getCurrentPlayerId().equals(currentPlayerId)) {
            if (getGame().getAwaitingDecision(selectingPlayerId) != null) {
                playerDecided(selectingPlayerId, "");
            }
            selectingPlayerId = getGame().getOpponent(selectingPlayerId);
        }
        if (getGame().getCurrentPlayerId().equals(currentPlayerId)) {
            assertEquals(initialTurnNumber, getGame().getGameState().getCurrentTurnNumber());
        } else {
            assertEquals(initialTurnNumber + 1, getGame().getGameState().getCurrentTurnNumber());
        }
    }


    default void skipDilemma() throws DecisionResultInvalidException, InvalidGameOperationException {
        for (String playerId : getGame().getAllPlayerIds())
            if (getGame().getCurrentPhase() == Phase.SEED_DILEMMA && getGame().getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
    }

    default void skipFacility() throws DecisionResultInvalidException, InvalidGameOperationException {
        for (String playerId : getGame().getAllPlayerIds())
            if (getGame().getCurrentPhase() == Phase.SEED_FACILITY && getGame().getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
    }
    


}