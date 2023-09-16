package com.gempukku.lotro.draft;

import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.tournament.TournamentCallback;

public interface Draft {
    void advanceDraft(TournamentCallback draftCallback);

    void playerChosenCard(String playerName, String cardId);

    void signUpForDraft(String playerName, DraftChannelVisitor draftChannelVisitor);

    DraftCommunicationChannel getCommunicationChannel(String playerName, int channelNumber)  throws SubscriptionExpiredException, SubscriptionConflictException;

    DraftCardChoice getCardChoice(String playerName);
    CardCollection getChosenCards(String player);

    boolean isFinished();
}
