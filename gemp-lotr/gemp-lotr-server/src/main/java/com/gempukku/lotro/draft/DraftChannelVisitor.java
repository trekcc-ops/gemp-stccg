package com.gempukku.lotro.draft;

import com.gempukku.lotro.game.CardCollection;

public interface DraftChannelVisitor {
    void channelNumber(int channelNumber);
    void timeLeft(long timeLeft);
    void cardChoice(CardCollection cardCollection);
    void noCardChoice();
    void chosenCards(CardCollection cardCollection);
}
