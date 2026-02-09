package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.UndockAction;
import com.gempukku.stccg.actions.placecard.AddCardsToSeedCardStackAction;
import com.gempukku.stccg.actions.placecard.RemoveCardsFromSeedCardStackAction;
import com.gempukku.stccg.actions.playcard.*;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    default void seedCard(String playerId, PhysicalCard cardToSeed) 
            throws DecisionResultInvalidException, InvalidGameOperationException {
        Action choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedCardAction seedAction &&
                        seedAction.getCardEnteringPlay() == cardToSeed)
                    choice = seedAction;
                if (action instanceof SeedOutpostAction seedAction &&
                        seedAction.getCardEnteringPlay() == cardToSeed)
                    choice = seedAction;
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    default void chooseOnlyAction(String playerId) throws DecisionResultInvalidException, InvalidGameOperationException {
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


    default void attemptMission(String playerId, AttemptingUnit attemptingUnit, MissionCard mission)
            throws DecisionResultInvalidException, InvalidGameLogicException, InvalidGameOperationException {
        AttemptMissionAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getLocationId() == mission.getLocationId())
                    choice = attemptAction;
            }
            choice.setAttemptingUnit(attemptingUnit);
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to attempt " + mission.getTitle());
    }

    default void attemptMission(String playerId, MissionLocation mission)
            throws DecisionResultInvalidException, InvalidGameLogicException, InvalidGameOperationException {
        AttemptMissionAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getLocationId() == mission.getLocationId())
                    choice = attemptAction;
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to attempt " + mission.getLocationName());
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

    default void playFacility(String playerId, PhysicalCard cardToSeed)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        PlayFacilityAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof PlayFacilityAction seedAction && seedAction.getCardEnteringPlay() == cardToSeed) {
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

    default void seedFacility(String playerId, PhysicalCard cardToSeed)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        SeedOutpostAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedOutpostAction seedAction && seedAction.getCardEnteringPlay() == cardToSeed) {
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
        SeedOutpostAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedOutpostAction seedAction && seedAction.getCardEnteringPlay() == cardToSeed) {
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

    default void reportCard(String playerId, PhysicalCard cardToReport, FacilityCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        ReportCardAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof ReportCardAction reportAction &&
                        reportAction.getCardEnteringPlay() == cardToReport) {
                    choice = reportAction;
                }
            }
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to report " + cardToReport.getTitle());
    }

    default void playCard(String playerId, PhysicalCard cardToPlay)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        PlayCardAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof PlayCardAction playCardAction &&
                        playCardAction.getCardEnteringPlay() == cardToPlay) {
                    choice = playCardAction;
                    break;
                } else if (
                        action instanceof SelectAndReportForFreeCardAction reportAction &&
                        reportAction.getSelectableReportables(getGame()).contains(cardToPlay)
                ) {
                    reportAction.setCardReporting(cardToPlay);
                    choice = reportAction;
                    break;
                }
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to play " + cardToPlay.getTitle());
    }

    default void beamCard(String playerId, PhysicalCard cardWithTransporters, ReportableCard cardToBeam,
                            PhysicalCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        BeamCardsAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof BeamCardsAction beamAction &&
                        beamAction.getCardUsingTransporters() == cardWithTransporters)
                    choice = beamAction;
            }
            choice.setOrigin(cardWithTransporters);
            choice.setCardsToMove(Collections.singletonList(cardToBeam));
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to beam " + cardToBeam.getTitle());
    }

    default void undockShip(String playerId, ShipCard ship)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        UndockAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof UndockAction undockAction &&
                        undockAction.getCardToMove() == ship)
                    choice = undockAction;
            }
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to undock " + ship.getTitle());
    }


    default void beamCards(String playerId, PhysicalCard cardWithTransporters,
                             Collection<? extends ReportableCard> cardsToBeam, PhysicalCard destination)
            throws DecisionResultInvalidException, InvalidGameOperationException {
        BeamCardsAction choice = null;
        AwaitingDecision decision = getGame().getAwaitingDecision(playerId);
        if (decision instanceof ActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof BeamCardsAction beamAction &&
                        beamAction.getCardUsingTransporters() == cardWithTransporters)
                    choice = beamAction;
            }
            choice.setOrigin(cardWithTransporters);
            choice.setCardsToMove(cardsToBeam);
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            getGame().removeDecision(playerId);
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null) {
            throw new DecisionResultInvalidException(
                    "No valid action to beam " + TextUtils.getConcatenatedCardLinks(cardsToBeam));
        }
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

    default void selectFirstAction(String player) throws DecisionResultInvalidException,
            InvalidGameOperationException {
        AwaitingDecision decision = getGame().getAwaitingDecision(player);
        if (ActionSelectionDecision.class.isAssignableFrom(decision.getClass())) {
            ActionSelectionDecision actionDecision = (ActionSelectionDecision) decision;
            getGame().removeDecision(player);
            try {
                actionDecision.selectFirstAction();
            } catch (DecisionResultInvalidException exp) {
                getGame().sendAwaitingDecision(decision);
                throw exp;
            }
            getGame().carryOutPendingActionsUntilDecisionNeeded();
        } else {
            throw new DecisionResultInvalidException();
        }
    }

    default void skipToNextTurnAndPhase(String turnPlayerName, Phase phase)
            throws InvalidGameOperationException, DecisionResultInvalidException {
        do {
            skipCardPlay();
            skipExecuteOrders();
        } while (!getGame().getCurrentPlayerId().equals(turnPlayerName));

        if (phase == Phase.EXECUTE_ORDERS) {
            skipCardPlay();
        }
    }


    default void skipCardPlay() throws DecisionResultInvalidException, InvalidGameOperationException {
        String playerId = getGame().getCurrentPlayerId();
        while (getGame().getCurrentPhase() == Phase.CARD_PLAY) {
            if (getGame().getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
        }
    }

    default void skipExecuteOrders() throws DecisionResultInvalidException, InvalidGameOperationException {
        String currentPlayerId = getGame().getCurrentPlayerId();
        String selectingPlayerId = currentPlayerId;
        while (getGame().getCurrentPhase() == Phase.EXECUTE_ORDERS && getGame().getCurrentPlayerId().equals(currentPlayerId)) {
            if (getGame().getAwaitingDecision(selectingPlayerId) != null) {
                playerDecided(selectingPlayerId, "");
            }
            selectingPlayerId = getGame().getOpponent(selectingPlayerId);
        }
    }


    default void skipDilemma() throws DecisionResultInvalidException, InvalidGameOperationException {
        for (String playerId : getGame().getAllPlayerIds())
            if (getGame().getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
    }

    default void skipFacility() throws DecisionResultInvalidException, InvalidGameOperationException {
        for (String playerId : getGame().getAllPlayerIds())
            if (getGame().getCurrentPhase() == Phase.SEED_FACILITY && getGame().getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
    }
    


}