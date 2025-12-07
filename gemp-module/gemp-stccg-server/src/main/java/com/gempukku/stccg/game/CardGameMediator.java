package com.gempukku.stccg.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.hall.GameSettings;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.player.PlayerClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class CardGameMediator {
    private static final long MILLIS_TO_SECONDS = 1000L;
    private static final Logger LOGGER = LogManager.getLogger(CardGameMediator.class);
    private static final String ERROR_MESSAGE = "Error processing game decision";
    private final Map<String, GameCommunicationChannel> _communicationChannels =
            Collections.synchronizedMap(new HashMap<>());
    final Map<String, CardDeck> _playerDecks = new HashMap<>();
    protected final Map<String, PlayerClock> _playerClocks = new HashMap<>();
    private final Map<String, Long> _decisionQuerySentTimes = new HashMap<>();
    private final Set<String> _playersPlaying = new HashSet<>();
    private final String _gameId;
    private final GameTimer _timeSettings;
    private final boolean _allowSpectators;
    private final boolean _showInGameHall;
    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    private int _channelNextIndex;
    private volatile boolean _destroyed;


    CardGameMediator(String gameId, GameParticipant[] participants, GameSettings gameSettings) {
        _allowSpectators = (gameSettings.getLeague() != null) ||
                (!gameSettings.isCompetitive() && !gameSettings.isPrivateGame() && !gameSettings.isHiddenGame());
        _gameId = gameId;
        _timeSettings = gameSettings.getTimeSettings();
        _showInGameHall = gameSettings.isHiddenGame();
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (GameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            CardDeck deck = participant.getDeck();
            _playerDecks.put(participantId, deck);
            _playerClocks.put(participantId, new PlayerClock(participantId, gameSettings.getTimeSettings()));
            _playersPlaying.add(participantId);
        }
    }

    public final boolean isVisibleToUser(String username) {
        return !_showInGameHall || _playersPlaying.contains(username);
    }

    public final boolean isDestroyed() {
        return _destroyed;
    }

    public final void destroy() {
        _destroyed = true;
    }

    public final String getGameId() {
        return _gameId;
    }

    public abstract DefaultGame getGame();

    public final boolean isAllowSpectators() {
        return _allowSpectators;
    }

    public final void setPlayerAutoPassSettings(User user, Set<Phase> phases) {
        String userId = user.getName();
        if (_playersPlaying.contains(userId)) {
            getGame().setPlayerAutoPassSettings(userId, phases);
        }
    }

    final void sendMessageToPlayers(String message) {
        getGame().sendMessage(message);
    }

    final void addGameStateListener(GameStateListener listener) {
        DefaultGame game = getGame();
        game.addGameStateListener(listener);
    }

    public final void addGameResultListener(GameResultListener listener) {
        getGame().addGameResultListener(listener);
    }

    public final String getWinner() {
        return getGame().getWinnerPlayerId();
    }

    public final List<String> getPlayersPlaying() {
        return new LinkedList<>(_playersPlaying);
    }

    public final String produceCardInfo(int cardId) throws JsonProcessingException {
        _readLock.lock();
        try {
            GameState gameState = getGame().getGameState();
            PhysicalCard card = gameState.findCardById(cardId);
            return getCardInfoJson(getGame(), card);
        } finally {
            _readLock.unlock();
        }
    }

    public final void startGame() {
        _writeLock.lock();
        try {
            getGame().startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public final void cleanup() {
        _writeLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            DefaultGame game = getGame();
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameStateListener channel = playerChannels.getValue();
                if (currentTime >
                        channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * MILLIS_TO_SECONDS) {
                    game.removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (game != null && game.getWinnerPlayerId() == null) {
                Map<String, Long> decisionTimes = new HashMap<>(_decisionQuerySentTimes);
                for (Map.Entry<String, Long> playerDecision : decisionTimes.entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * MILLIS_TO_SECONDS) {
                        addTimeSpentOnDecisionToUserClock(player);
                        game.playerLost(player, "Player decision timed-out");
                    }
                }

                for (PlayerClock playerClock : _playerClocks.values()) {
                    String player = playerClock.getPlayerId();
                    if (_timeSettings.maxSecondsPerPlayer() -
                            playerClock.getTimeElapsed() - getCurrentUserPendingTime(player) < 0) {
                        addTimeSpentOnDecisionToUserClock(player);
                        game.playerLost(player, "Player run out of time");
                    }
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public final void concede(User player) {
        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (getGame().getWinnerPlayerId() == null && _playersPlaying.contains(playerId)) {
                addTimeSpentOnDecisionToUserClock(playerId);
                getGame().playerLost(playerId, "Concession");
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public final void cancel(User player) {

        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_playersPlaying.contains(playerId))
                getGame().requestCancel(playerId);
        } finally {
            _writeLock.unlock();
        }
    }

    public final synchronized void playerAnswered(User player, int channelNumber, int decisionId, String answer)
            throws HttpProcessingException {
        String playerName = player.getName();
        _writeLock.lock();
        try {
            GameStateListener communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel == null)
                throw new SubscriptionExpiredException();
            if (communicationChannel.getChannelNumber() != channelNumber)
                throw new SubscriptionConflictException();
            DefaultGame game = getGame();
            AwaitingDecision awaitingDecision = game.getAwaitingDecision(playerName);

            if (awaitingDecision != null) {
                if (awaitingDecision.getDecisionId() == decisionId && !game.isFinished()) {
                    GameState gameState = game.getGameState();
                    try {
                        gameState.playerDecisionFinished(playerName, game.getUserFeedback());
                        awaitingDecision.decisionMade(answer);

                        // Decision successfully made, add the time to user clock
                        addTimeSpentOnDecisionToUserClock(playerName);

                        game.carryOutPendingActionsUntilDecisionNeeded();
                        startClocksForUsersPendingDecision();

                    } catch (DecisionResultInvalidException exp) {
                        /* Participant provided wrong answer - send a warning message,
                        and ask again for the same decision */
                        game.sendWarning(playerName, exp.getWarningMessage());
                        game.sendAwaitingDecision(awaitingDecision);
                    } catch (InvalidGameOperationException | RuntimeException runtimeException) {
                        LOGGER.error(ERROR_MESSAGE, runtimeException);
                        game.cancelGame();
                    }
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }


    public final GameCommunicationChannel getCommunicationChannel(User player, int channelNumber)
            throws HttpProcessingException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel == null)
                throw new SubscriptionExpiredException();

            if (communicationChannel.getChannelNumber() == channelNumber)
                return communicationChannel;
            else
                throw new SubscriptionConflictException();

        } finally {
            _readLock.unlock();
        }
    }


    public final String serializeEventsToString(GameCommunicationChannel communicationChannel)
            throws IOException {
        _readLock.lock();
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writeValueAsString(communicationChannel);
        } catch(IOException exp) {
            getGame().sendErrorMessage("Unable to serialize game events");
            throw new IOException(exp.getMessage());
        } finally {
            _readLock.unlock();
        }
    }

    public final String signupUserForGameAndGetGameState(User player)
            throws PrivateInformationException, JsonProcessingException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();
        GameCommunicationChannel channel;
        int channelNumber;

        _readLock.lock();
        try {
            channelNumber = _channelNextIndex;
            _channelNextIndex++;

            channel = new GameCommunicationChannel(getGame(), playerName, channelNumber);
            _communicationChannels.put(playerName, channel);
            addGameStateListener(channel);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = getGame().getGameState().serializeForPlayer(player.getName());
            JsonNode gameState = mapper.readTree(jsonString);
            Map<String, Object> result = new HashMap<>();
            result.put("channelNumber", channelNumber);
            result.put("gameState", gameState);
            String resultString = mapper.writeValueAsString(result);
            return resultString;
        } finally {
            _readLock.unlock();
        }
    }



    public final GameCommunicationChannel signupUserForGameAndGetChannel(User player)
            throws PrivateInformationException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();
        GameCommunicationChannel channel;
        int channelNumber;

        _readLock.lock();
        try {
            channelNumber = _channelNextIndex;
            _channelNextIndex++;

            channel = new GameCommunicationChannel(getGame(), playerName, channelNumber);
            _communicationChannels.put(playerName, channel);
            addGameStateListener(channel);
            return channel;
        } finally {
            _readLock.unlock();
        }
    }


    private void startClocksForUsersPendingDecision() {
        long currentTime = System.currentTimeMillis();
        Set<String> users = getGame().getUsersPendingDecision();
        for (String user : users)
            _decisionQuerySentTimes.put(user, currentTime);
    }

    private void addTimeSpentOnDecisionToUserClock(String participantId) {
        Long queryTime = _decisionQuerySentTimes.remove(participantId);
        if (queryTime != null) {
            long currentTime = System.currentTimeMillis();
            long diffSec = (currentTime - queryTime) / 1000;
            //noinspection NumericCastThatLosesPrecision
            PlayerClock playerClock = _playerClocks.get(participantId);
            playerClock.addElapsedTime((int) diffSec);
        }
    }

    private int getCurrentUserPendingTime(String participantId) {
        int result = 0;
        if (_decisionQuerySentTimes.containsKey(participantId)) {
            long queryTime = _decisionQuerySentTimes.get(participantId);
            long currentTime = System.currentTimeMillis();
            //noinspection NumericCastThatLosesPrecision
            result = (int) ((currentTime - queryTime) / 1000);
        }
        return result;
    }

    final GameTimer getTimeSettings() {
        return _timeSettings;
    }

    final Map<String, Integer> getPlayerClocks() {
        Map<String, Integer> result = new HashMap<>();
        for (String playerId : _playerClocks.keySet()) {
            result.put(playerId, _playerClocks.get(playerId).getTimeElapsed());
        }
        return result;
    }

    public String serializeCompleteGameState() throws JsonProcessingException {
        _readLock.lock();
        try {
            GameState gameState = getGame().getGameState();
            return gameState.serializeComplete();
        } finally {
            _readLock.unlock();
        }
    }

    public String serializeGameStateForPlayer(String playerId) throws JsonProcessingException {
        _readLock.lock();
        try {
            GameState gameState = getGame().getGameState();
            return gameState.serializeForPlayer(playerId);
        } finally {
            _readLock.unlock();
        }
    }

    private String getCardInfoJson(DefaultGame cardGame, PhysicalCard card) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Object> itemsToSerialize = new HashMap<>();
        if (card == null || (!card.isInPlay() && !card.isInHand(cardGame)))
            return mapper.writeValueAsString(itemsToSerialize);

        Collection<String> modifiersToAdd = new ArrayList<>();
        for (Modifier modifier : cardGame.getGameState().getModifiersQuerying().getModifiersAffecting(card)) {
            if (modifier != null && !Objects.equals(modifier.getCardInfoText(getGame(), card), "null")) {
                modifiersToAdd.add(modifier.getCardInfoText(getGame(), card));
            }
        }
        itemsToSerialize.put("modifiers", modifiersToAdd);

        if (card instanceof ST1EPhysicalCard stCard) {
            itemsToSerialize.put("isStopped", stCard.isStopped());
        }

        List<String> affiliationTexts = new ArrayList<>();
        if (card instanceof AffiliatedCard affiliatedCard) {
            for (Affiliation affiliation : Affiliation.values()) {
                if (affiliatedCard.isAffiliation(affiliation)) {
                    affiliationTexts.add(affiliation.name());
                }
            }
        }
        itemsToSerialize.put("affiliations", affiliationTexts);
        
        List<String> cardIconTexts = new ArrayList<>();
        for (CardIcon icon : CardIcon.values()) {
            if (card.hasIcon(getGame(), icon)) {
                cardIconTexts.add(icon.toHTML());
            }
        }
        itemsToSerialize.put("icons", cardIconTexts);
        

        List<Map<Object, Object>> crew = new ArrayList<>();
        if (card instanceof CardWithCrew cardWithCrew) {
            for (PhysicalCard crewCard : cardWithCrew.getCrew()) {
                crew.add(getCardProperties(crewCard));
            }
        }
        itemsToSerialize.put("crew", crew);

        List<Map<Object, Object>> dockedCards = new ArrayList<>();
        if (card instanceof FacilityCard facility) {
            for (PhysicalCard ship : facility.getDockedShips()) {
                dockedCards.add(getCardProperties(ship));
            }
        }
        itemsToSerialize.put("dockedCards", dockedCards);

        if (card instanceof PhysicalShipCard ship) {
            List<String> staffingRequirements = new ArrayList<>();
            if (!ship.getStaffingRequirements().isEmpty()) {
                for (CardIcon icon : ship.getStaffingRequirements()) {
                    staffingRequirements.add(icon.toHTML());
                }
            }
            itemsToSerialize.put("staffingRequirements", staffingRequirements);

            itemsToSerialize.put("isStaffed", ship.isStaffed());
            itemsToSerialize.put("printedRange", ship.getBlueprint().getRange());
            itemsToSerialize.put("rangeAvailable", ship.getRangeAvailable());
        }

        if (card instanceof MissionCard mission) {
            ST1EGame stGame = mission.getGame();
            itemsToSerialize.put("missionRequirements", mission.getMissionRequirements());

            List<Map<Object, Object>> serializableAwayTeams = new ArrayList<>();
            if (mission.getGameLocation() instanceof MissionLocation missionLocation && missionLocation.isPlanet()) {
                List<AwayTeam> awayTeamsOnPlanet = missionLocation.getAwayTeamsOnSurface(stGame).toList();
                for (AwayTeam team : awayTeamsOnPlanet) {
                    Map<Object, Object> awayTeamInfo = new HashMap<>();
                    awayTeamInfo.put("playerId", team.getPlayerId());
                    List<Map<Object, Object>> awayTeamMembers = new ArrayList<>();
                    for (PhysicalCard member : team.getCards()) {
                        awayTeamMembers.add(getCardProperties(member));
                    }
                    awayTeamInfo.put("cardsInAwayTeam", awayTeamMembers);
                    serializableAwayTeams.add(awayTeamInfo);
                }
            }
            itemsToSerialize.put("awayTeams", serializableAwayTeams);
        }

        return mapper.writeValueAsString(itemsToSerialize);
    }

    private static Map<Object, Object> getCardProperties(PhysicalCard card) {
        List<CardType> cardTypesShowingUniversal = new ArrayList<>();
        cardTypesShowingUniversal.add(CardType.PERSONNEL);
        cardTypesShowingUniversal.add(CardType.SHIP);
        cardTypesShowingUniversal.add(CardType.FACILITY);
        cardTypesShowingUniversal.add(CardType.SITE);


        Map<Object, Object> cardMap = new HashMap<>();
        cardMap.put("title", card.getTitle());
        cardMap.put("cardId", card.getCardId());
        cardMap.put("blueprintId", card.getBlueprintId());
        cardMap.put("uniqueness", card.getUniqueness().name());
        cardMap.put("cardType", card.getCardType().name());
        cardMap.put("imageUrl", card.getImageUrl());
        boolean hasUniversalIcon = card.isUniversal() &&
                cardTypesShowingUniversal.contains(card.getCardType());
        cardMap.put("hasUniversalIcon", hasUniversalIcon);

        return cardMap;
    }
}