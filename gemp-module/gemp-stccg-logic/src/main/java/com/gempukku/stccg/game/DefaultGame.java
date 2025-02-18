package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.gameevent.ActionResultGameEvent;
import com.gempukku.stccg.gameevent.FlashCardInPlayGameEvent;
import com.gempukku.stccg.gameevent.GameEvent;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.*;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.modifiers.ModifiersEnvironment;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public abstract class DefaultGame {
    private static final int LAST_MESSAGE_STORED_COUNT = 15;

    // Game parameters
    protected final GameFormat _format;
    protected final CardBlueprintLibrary _library;
    // IRL game mechanics
    protected final Set<String> _allPlayerIds;
    protected TurnProcedure _turnProcedure;
    final List<String> _lastMessages = new LinkedList<>();

    // Endgame operations
    protected final Set<String> _requestedCancel = new HashSet<>();
    protected boolean _cancelled;
    protected boolean _finished;
    protected String _winnerPlayerId;
    protected final Map<String, String> _losers = new HashMap<>();
    // Game code infrastructure
    protected final Set<GameResultListener> _gameResultListeners = new HashSet<>();
    protected final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();
    protected final UserFeedback _userFeedback;
    private final List<GameSnapshot> _snapshots = new LinkedList<>();
    protected GameSnapshot _snapshotToRestore;
    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private int _nextSnapshotId;
    private final static int NUM_PREV_TURN_SNAPSHOTS_TO_KEEPS = 1;
    protected final GameType _gameType;

    public DefaultGame(GameFormat format, Map<String, CardDeck> decks, final CardBlueprintLibrary library,
                       GameType gameType) {
        _format = format;
        _userFeedback = new DefaultUserFeedback(this);
        _library = library;
        _allPlayerIds = decks.keySet();
        _gameType = gameType;
    }


    public abstract GameState getGameState();
    public boolean shouldAutoPass(Phase phase) {
        return false;
    }

    public GameFormat getFormat() {
        return _format;
    }

    public Set<String> getPlayerIds() { return _allPlayerIds; }

    public Collection<Player> getPlayers() { return getGameState().getPlayers(); }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }
    public void addGameStateListener(String playerId, GameStateListener listener) {
        _gameStateListeners.add(listener);
/*        try {
            GameState gameState = getGameState();
            PlayerOrder playerOrder = gameState.getPlayerOrder();
            if (playerOrder != null) {
                listener.initializeBoard();
                if (getCurrentPlayerId() != null) {
                    sendActionResultToClient(); // for current player
                }

                try {

                    Player player = getPlayer(playerId);
                    boolean sharedMission;
                    Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(getGameState().getAllCardsInPlay());
                    Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

                    // Send missions in order
                    for (MissionLocation location : ((ST1EGameState) getGameState()).getSpacelineLocations()) {
                        for (int i = 0; i < location.getMissionCards().size(); i++) {
                            sharedMission = i != 0;
                            // TODO SNAPSHOT - Pretty sure this sendCreatedCardToListener function won't work with snapshotting
                            PhysicalCard mission = location.getMissionCards().get(i);
                            getGameState().sendCreatedCardToListener(mission, sharedMission, listener);
                            cardsLeftToSend.remove(mission);
                            sentCardsFromPlay.add(mission);
                        }
                    }

                    int cardsToSendAtLoopStart;
                    do {
                        cardsToSendAtLoopStart = cardsLeftToSend.size();
                        Iterator<PhysicalCard> cardIterator = cardsLeftToSend.iterator();
                        while (cardIterator.hasNext()) {
                            PhysicalCard physicalCard = cardIterator.next();
                            PhysicalCard attachedTo = physicalCard.getAttachedTo();
                            if (physicalCard.isPlacedOnMission()) {
                                GameLocation location = physicalCard.getGameLocation();
                                if (location instanceof MissionLocation mission) {
                                    PhysicalCard topMission = mission.getTopMissionCard();
                                    if (sentCardsFromPlay.contains(topMission)) {
                                        getGameState().sendCreatedCardToListener(physicalCard, false, listener);
                                        sentCardsFromPlay.add(physicalCard);

                                        cardIterator.remove();
                                    }
                                } else {
                                    throw new InvalidGameLogicException("Card placed on mission, but is attached to a non-mission card");
                                }
                            } else if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                                getGameState().sendCreatedCardToListener(physicalCard, false, listener);
                                sentCardsFromPlay.add(physicalCard);

                                cardIterator.remove();
                            }
                        }
                    } while (cardsToSendAtLoopStart != cardsLeftToSend.size() && !cardsLeftToSend.isEmpty());

                    for (PhysicalCard physicalCard : player.getCardGroupCards(Zone.HAND)) {
                        getGameState().sendCreatedCardToListener(physicalCard, false, listener);
                    }

                    List<PhysicalCard> missionPile = player.getCardGroupCards(Zone.MISSIONS_PILE);
                    if (missionPile != null) {
                        for (PhysicalCard physicalCard : missionPile) {
                            getGameState().sendCreatedCardToListener(physicalCard, false, listener);
                        }
                    }

                    for (PhysicalCard physicalCard : player.getCardGroupCards(Zone.DISCARD)) {
                        getGameState().sendCreatedCardToListener(physicalCard, false, listener);
                    }


                } catch (PlayerNotFoundException | InvalidGameLogicException | InvalidGameOperationException exp) {
                    sendErrorMessage(exp);
                    cancelGame();
                }
            }
            for (String lastMessage : getMessages())
                listener.sendMessage(lastMessage);

            final AwaitingDecision awaitingDecision = gameState.getDecision(playerId);
            gameState.sendAwaitingDecisionToListener(listener, playerId, awaitingDecision);
        } catch(PlayerNotFoundException exp) {
            sendErrorMessage(exp);
        } */
    }

    public Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    public void initializePlayerOrder(PlayerOrder playerOrder) throws PlayerNotFoundException {
        getGameState().initializePlayerOrder(playerOrder);
        for (GameStateListener listener : _gameStateListeners) {
            listener.initializeBoard();
        }
    }


    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayerIds.size() && !_finished) {
            _cancelled = true;

            if (getGameState() != null)
                sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (getGameState() != null) {
                sendMessage(
                        "Game was cancelled due to an error, the error was logged and will be fixed soon.");
                sendMessage(
                        "Please post the replay game link and description of what happened on the tech support forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void setCurrentPhase(Phase phase) {
        getGameState().setCurrentPhase(phase);
        sendActionResultToClient();
    }



    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayerIds);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    protected void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;
        if (getGameState() != null)
            sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        assert getGameState() != null;

        for (GameResultListener gameResultListener : _gameResultListeners) {
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);
        }

        _finished = true;
    }

    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (getGameState() != null)
                    sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayerIds.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayerIds);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.getFirst(), "Last remaining player in game");
                }
            }
        }
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }
    public CardBlueprintLibrary getBlueprintLibrary() {
        return _library;
    }

    public ActionsEnvironment getActionsEnvironment() {
        return getGameState().getActionsEnvironment();
    }

    public ModifiersEnvironment getModifiersEnvironment() {
        return getGameState().getModifiersLogic();
    }

    public abstract TurnProcedure getTurnProcedure();

    public void startGame() {
        try {
            if (!_cancelled)
                getTurnProcedure().carryOutPendingActionsUntilDecisionNeeded();
        } catch(PlayerNotFoundException | InvalidGameLogicException | InvalidGameOperationException |
                CardNotFoundException exp) {
            sendErrorMessage(exp);
        }
    }

    public void carryOutPendingActionsUntilDecisionNeeded() throws InvalidGameOperationException {
        try {
            if (!_cancelled) {
                getTurnProcedure().carryOutPendingActionsUntilDecisionNeeded();

                while (_snapshotToRestore != null) {
//                restoreSnapshot();
                    carryOutPendingActionsUntilDecisionNeeded();
                }
            }
        } catch(PlayerNotFoundException | InvalidGameLogicException | CardNotFoundException exp) {
            throw new InvalidGameOperationException(exp);
        }
    }

    public Player getPlayer(int index) throws PlayerNotFoundException {
        return getPlayer(getPlayerId(index));
    }

    public String getPlayerId(int index) {
        return getAllPlayerIds()[index - 1];
    }

    public Player getPlayer(String playerId) throws PlayerNotFoundException {
        return getGameState().getPlayer(playerId);
    }

    public String[] getAllPlayerIds() {
        return _allPlayerIds.toArray(new String[0]);
    }

    public Player getCurrentPlayer() throws PlayerNotFoundException {
        GameState gameState = getGameState();
        return gameState.getCurrentPlayer();
    }

    public List<GameSnapshot> getSnapshots() {
        return Collections.unmodifiableList(_snapshots);
    }

    public String getOpponent(String playerId) {
            // TODO - Only works for 2-player games
            return getAllPlayerIds()[0].equals(playerId) ?
                    getAllPlayerIds()[1] : getAllPlayerIds()[0];
    }

    public Player getOpponent(Player player) throws PlayerNotFoundException {
        return getPlayer(getOpponent(player.getPlayerId()));
    }

    public void requestRestoreSnapshot(int snapshotId) {
        if (_snapshotToRestore == null) {
            for (Iterator<GameSnapshot> iterator = _snapshots.iterator(); iterator.hasNext();) {
                GameSnapshot gameSnapshot = iterator.next();
                if (gameSnapshot.getId() == snapshotId) {
                    _snapshotToRestore = gameSnapshot;
                }
                // After snapshot to restore is found, remove any snapshots after it from list
                if (_snapshotToRestore != null) {
                    // Remove the current snapshot from the iterator and the list.
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Determines if a snapshot is pending to be restored.
     * @return true or false
     */
    public boolean isRestoreSnapshotPending() {
        return _snapshotToRestore != null;
    }

    /**
     * Creates a snapshot of the current state of the game.
     * @param description the description
     */
    public void takeSnapshot(String description) {
        // TODO - Star Wars code used PlayCardStates here
        try {
            pruneSnapshots();
        } catch(PlayerNotFoundException exp) {
            sendErrorMessage(exp);
            sendErrorMessage("Unable to prune game state snapshots");
        }
        // need to specifically exclude when getPlayCardStates() is not empty to allow for battles to be initiated by interrupts
        ++_nextSnapshotId;
        // TODO
//        _snapshots.add(new GameSnapshot(_nextSnapshotId, description, getGameState()));
    }

    /**
     * Prunes older snapshots.
     */
    private void pruneSnapshots() throws PlayerNotFoundException {
        // Remove old snapshots until reaching snapshots to keep
        for (Iterator<GameSnapshot> iterator = _snapshots.iterator(); iterator.hasNext();) {
            GameSnapshot gameSnapshot = iterator.next();
            int snapshotCurrentTurnNumber = gameSnapshot.getCurrentTurnNumber();
            int currentTurnNumber = getGameState().getCurrentTurnNumber();
            if (snapshotCurrentTurnNumber <= 1 && currentTurnNumber <= 1) {
                break;
            }
            int pruneOlderThanTurn = currentTurnNumber - NUM_PREV_TURN_SNAPSHOTS_TO_KEEPS;
            if (snapshotCurrentTurnNumber >= pruneOlderThanTurn) {
                break;
            }
            // Remove the current snapshot from the iterator and the list.
            iterator.remove();
        }
    }
    
    public void sendMessage(String message) {
        addMessage(message);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendMessage(message);
    }

    public Phase getCurrentPhase() { return getGameState().getCurrentPhase(); }

    public String getCurrentPlayerId() { return getGameState().getCurrentPlayerId(); }

    public AwaitingDecision getAwaitingDecision(String playerName) {
        return _userFeedback.getAwaitingDecision(playerName);
    }

    public Set<String> getUsersPendingDecision() {
        return _userFeedback.getUsersPendingDecision();
    }

    public void sendAwaitingDecision(AwaitingDecision awaitingDecision) {
        _userFeedback.sendAwaitingDecision(awaitingDecision);
    }

    public String getStatus() {
        final Phase currentPhase = getCurrentPhase();
        String gameStatus;
        if (_cancelled)
            gameStatus = "Cancelled";
        else if (_finished)
            gameStatus = "Finished";
        else if (currentPhase.isSeedPhase())
            gameStatus = "Seeding";
        else gameStatus = "Playing";
        return gameStatus;
    }

    public boolean isDiscardPilePublic() {
        return _format.discardPileIsPublic();
    }

    public boolean isCarryingOutEffects() {
        return _userFeedback.hasNoPendingDecisions() && _winnerPlayerId == null && !isRestoreSnapshotPending();
    }

    public PhysicalCard getCardFromCardId(int cardId) throws CardNotFoundException {
        return getGameState().getCardFromCardId(cardId);
    }

    public List<String> getMessages() { return _lastMessages; }

    public void addMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
    }

    public void sendErrorMessage(Exception exp) {
        String message = "ERROR: " + exp.getMessage();
        sendMessage(message);
    }

    public void sendErrorMessage(String message) {
        sendMessage("ERROR: " + message);
    }

    public Action getActionById(int actionId) {
        ActionsEnvironment environment = getActionsEnvironment();
        return environment.getActionById(actionId);
    }

    public void activatedCard(Player performingPlayer, PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners()) {
            GameEvent event = new FlashCardInPlayGameEvent(card, performingPlayer);
            listener.sendEvent(event);
        }
    }


    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public void sendActionResultToClient() {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new ActionResultGameEvent(getGameState(), listener.getPlayerId()));
    }


    public void performRevert(Player player) {
        DefaultGame thisGame = this;
        String playerId = player.getPlayerId();
        final List<Integer> snapshotIds = new ArrayList<>();
        final List<String> snapshotDescriptions = new ArrayList<>();
        for (GameSnapshot gameSnapshot : getSnapshots()) {
            snapshotIds.add(gameSnapshot.getId());
            snapshotDescriptions.add(gameSnapshot.getDescription());
        }
        int numSnapshots = snapshotDescriptions.size();
        if (numSnapshots == 0) {
            checkPlayerAgain();
            return;
        }
        snapshotIds.add(-1);
        snapshotDescriptions.add("Do not revert");

        // Ask player to choose snapshot to revert back to
        getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(player, "Choose game state to revert prior to",
                        snapshotDescriptions.toArray(new String[0]), snapshotDescriptions.size() - 1, this) {
                    @Override
                    public void validDecisionMade(int index, String result) throws DecisionResultInvalidException {
                        try {
                            final int snapshotIdChosen = snapshotIds.get(index);
                            if (snapshotIdChosen == -1) {
                                checkPlayerAgain();
                                return;
                            }

                            sendMessage(playerId + " attempts to revert game to a previous state");

                            // Confirm with the other player if it is acceptable to revert to the game state
                            // TODO SNAPSHOT - Needs to work differently if more than 2 players
                            final String opponent;
                            String temp_opponent;
                            temp_opponent = getOpponent(playerId);

                            opponent = temp_opponent;
                            Player opponentPlayer = getPlayer(opponent);

                            StringBuilder snapshotDescMsg = new StringBuilder("</br>");
                            for (int i = 0; i < snapshotDescriptions.size() - 1; ++i) {
                                if (i == index) {
                                    snapshotDescMsg.append("</br>").append(">>> Revert to here <<<");
                                }
                                if ((index - i) < 3) {
                                    snapshotDescMsg.append("</br>").append(snapshotDescriptions.get(i));
                                }
                            }
                            snapshotDescMsg.append("</br>");

                            getUserFeedback().sendAwaitingDecision(
                                    new YesNoDecision(opponentPlayer,
                                            "Do you want to allow game to be reverted to the following game state?" +
                                                    snapshotDescMsg, thisGame) {
                                        @Override
                                        protected void yes() {
                                            sendMessage(opponent + " allows game to revert to a previous state");
                                            requestRestoreSnapshot(snapshotIdChosen);
                                        }

                                        @Override
                                        protected void no() {
                                            sendMessage(opponent + " denies attempt to revert game to a previous state");
                                            checkPlayerAgain();
                                        }
                                    });
                        } catch(PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });
    }


    /**
     * This method if the same player should be asked again to choose an action or pass.
     */
    private GameProcess checkPlayerAgain() {
        // TODO SNAPSHOT - The SWCCG code is incompatible with the structure of this process
/*        _playOrder.getNextPlayer();
        return new PlayersPlayPhaseActionsInOrderGameProcess(
                getGameState().getPlayerOrder().getPlayOrder(
                        _playOrder.getNextPlayer(), true), _consecutivePasses, _followingGameProcess); */
        return null;
    }


    public GameType getGameType() {
        return _gameType;
    }

}