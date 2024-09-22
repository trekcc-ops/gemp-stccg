package com.gempukku.stccg.draft;

import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.cards.GenericCardItem;

public class DraftCommunicationChannel implements LongPollableResource {
    private final int _channelNumber;
    private long _lastAccessed;

    private String _cardChoiceOnClient;
    private volatile boolean _changed;
    private volatile WaitingRequest _waitingRequest;

    public DraftCommunicationChannel(int channelNumber) {
        _channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    public long getLastAccessed() {
        return _lastAccessed;
    }

    private void updateLastAccess() {
        _lastAccessed = System.currentTimeMillis();
    }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    public synchronized void draftChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public boolean hasChangesInCommunicationChannel(DraftCardChoice draftCardChoice) {
        updateLastAccess();

        CardCollection cardCollection = draftCardChoice.getCardCollection();
        if (cardCollection == null)
            return _cardChoiceOnClient != null;
        return  !getSerialized(cardCollection).equals(_cardChoiceOnClient);
    }

    private String getSerialized(CardCollection cardCollection) {
        StringBuilder sb = new StringBuilder();
        for (GenericCardItem collectionItem : cardCollection.getAll())
            sb.append(collectionItem.getCount()).append("x").append(collectionItem.getBlueprintId()).append("|");

        return sb.toString();
    }

    public synchronized void processCommunicationChannel(DraftCardChoice draftCardChoice, CardCollection chosenCards, DraftChannelVisitor draftChannelVisitor) {
        updateLastAccess();

        CardCollection cardCollection = draftCardChoice.getCardCollection();
        if (cardCollection != null) {
            draftChannelVisitor.timeLeft(draftCardChoice.getTimeLeft());
            draftChannelVisitor.cardChoice(cardCollection);
            _cardChoiceOnClient = getSerialized(cardCollection);
        } else {
            draftChannelVisitor.noCardChoice();
            _cardChoiceOnClient = null;
        }
        draftChannelVisitor.chosenCards(chosenCards);

        _changed = false;
    }
}
