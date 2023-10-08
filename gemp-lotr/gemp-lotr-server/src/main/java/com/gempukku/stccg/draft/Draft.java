package com.gempukku.stccg.draft;

import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.game.CardCollection;
import com.gempukku.stccg.tournament.TournamentCallback;

public interface Draft {
    void advanceDraft(TournamentCallback draftCallback);

    void playerChosenCard(String playerName, String cardId);

    void signUpForDraft(String playerName, DraftChannelVisitor draftChannelVisitor);

    DraftCommunicationChannel getCommunicationChannel(String playerName, int channelNumber)  throws SubscriptionExpiredException, SubscriptionConflictException;

    DraftCardChoice getCardChoice(String playerName);
    CardCollection getChosenCards(String player);

    boolean isFinished();
}
