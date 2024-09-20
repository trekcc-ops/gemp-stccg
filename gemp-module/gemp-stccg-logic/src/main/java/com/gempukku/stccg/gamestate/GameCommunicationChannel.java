package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.common.LongPollableResource;
import com.gempukku.stccg.common.WaitingRequest;

import java.util.*;

import static com.gempukku.stccg.gamestate.GameEvent.Type.*;

public class GameCommunicationChannel implements GameStateListener, LongPollableResource {
    private List<GameEvent> _events = Collections.synchronizedList(new LinkedList<>());
    private final String _playerId;
    private long _lastConsumed = System.currentTimeMillis();
    private final int _channelNumber;
    private volatile WaitingRequest _waitingRequest;

    private final GameFormat _format;

    public GameCommunicationChannel(String self, int channelNumber, GameFormat format) {
        _playerId = self;
        _channelNumber = channelNumber;
        _format = format;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    @Override
    public void initializeBoard(List<String> participants, boolean discardIsPublic) {
        List<String> participantIds = new LinkedList<>(participants);
        appendEvent(new GameEvent(PARTICIPANTS)
                .participantId(_playerId)
                .allParticipantIds(participantIds)
                .discardPublic(discardIsPublic)
        );
    }

    public String getPlayerId() { return _playerId; }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (!_events.isEmpty())
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    private synchronized void appendEvent(GameEvent event) {
        _events.add(event);
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    private int[] getCardIds(Collection<PhysicalCard> cards) {
        int[] result = new int[cards.size()];
        int index = 0;
        for (PhysicalCard card : cards) {
            result[index] = card.getCardId();
            index++;
        }
        return result;
    }

    @Override
    public void setCurrentPhase(String phase) {
        appendEvent(new GameEvent(GAME_PHASE_CHANGE).phase(phase));
    }

    @Override
    public void cardCreated(PhysicalCard card, GameEvent.Type eventType) {
        boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
        if (card.getZone().isPublic() || publicDiscard ||
                (card.getZone().isVisibleByOwner() && card.getOwnerName().equals(_playerId)))
            appendEvent(new GameEvent(eventType).card(card));
    }

    public void putCardIntoPlay(PhysicalCard card, boolean restoreSnapshot) {
        if (restoreSnapshot)
            cardCreated(card, PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING);
        else cardCreated(card, PUT_CARD_INTO_PLAY);
    }


    @Override
    public void cardCreated(PhysicalCard card, boolean overridePlayerVisibility) {
        boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
        if (card.getZone().isPublic() || publicDiscard || ((overridePlayerVisibility ||
                card.getZone().isVisibleByOwner()) && card.getOwnerName().equals(_playerId)))
            appendEvent(new GameEvent(PUT_CARD_INTO_PLAY).card(card));
    }

    @Override
    public void cardMoved(PhysicalCard card) {
        appendEvent(new GameEvent(MOVE_CARD_IN_PLAY).card(card));
    }

    @Override
    public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards) {
        Set<PhysicalCard> removedCardsVisibleByPlayer = new HashSet<>();
        for (PhysicalCard card : cards) {
            boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
            if (card.getZone().isPublic() || publicDiscard ||
                    (card.getZone().isVisibleByOwner() && card.getOwnerName().equals(_playerId)))
                removedCardsVisibleByPlayer.add(card);
        }
        if (!removedCardsVisibleByPlayer.isEmpty())
            appendEvent(new GameEvent(REMOVE_CARD_FROM_PLAY).otherCardIds(getCardIds(removedCardsVisibleByPlayer))
                    .participantId(playerPerforming));
    }

    @Override
    public void setPlayerDecked(String participant, boolean bool) {
        appendEvent(new GameEvent(PLAYER_DECKED).participantId(participant).bool(bool));
    }

    @Override
    public void setPlayerScore(String participant, int points) {
        appendEvent(new GameEvent(PLAYER_SCORE).participantId(participant).score(points));
    }

    @Override
    public void setTribbleSequence(String tribbleSequence) {
        appendEvent(new GameEvent(TRIBBLE_SEQUENCE_UPDATE).message(tribbleSequence));
    }

    @Override
    public void setCurrentPlayerId(String currentPlayerId) {
        appendEvent(new GameEvent(TURN_CHANGE).participantId(currentPlayerId));
    }

    @Override
    public void sendMessage(String message) {
        appendEvent(new GameEvent(SEND_MESSAGE).message(message));
    }

    @Override
    public void sendGameStats(GameStats gameStats) {
        appendEvent(new GameEvent(GAME_STATS).gameStats(gameStats.makeACopy()));
    }

    @Override
    public void cardAffectedByCard(String performingPlayerId, PhysicalCard card,
                                   Collection<PhysicalCard> affectedCards) {
        appendEvent(new GameEvent(CARD_AFFECTED_BY_CARD).card(card).participantId(performingPlayerId)
                .otherCardIds(getCardIds(affectedCards)));
    }

    @Override
    public void eventPlayed(PhysicalCard card) {
        appendEvent(new GameEvent(SHOW_CARD_ON_SCREEN).card(card));
    }

    @Override
    public void cardActivated(String playerPerforming, PhysicalCard card) {
        appendEvent(new GameEvent(FLASH_CARD_IN_PLAY).card(card).participantId(playerPerforming));
    }

    public void decisionRequired(String playerId, AwaitingDecision decision) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(DECISION).awaitingDecision(decision).participantId(playerId));
    }

    @Override
    public void sendWarning(String playerId, String warning) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(SEND_WARNING).message(warning));
    }

    public List<GameEvent> consumeGameEvents() {
        updateLastAccess();
        List<GameEvent> result = _events;
        _events = Collections.synchronizedList(new LinkedList<>());
        return result;
    }

    public boolean hasGameEvents() {
        updateLastAccess();
        return !_events.isEmpty();
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return _lastConsumed;
    }

    @Override
    public void endGame() {
        appendEvent(new GameEvent(GAME_ENDED));
    }

    @Override
    public void cardImageUpdated(PhysicalCard card) {
        appendEvent(new GameEvent(UPDATE_CARD_IMAGE).card(card));
    }
}
