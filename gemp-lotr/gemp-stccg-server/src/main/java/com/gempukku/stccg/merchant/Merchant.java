package com.gempukku.stccg.merchant;

import java.util.Date;

public interface Merchant {
    Integer getCardSellPrice(String blueprintId, Date currentTime);

    Integer getCardBuyPrice(String blueprintId, Date currentTime);

    /**
     * Called when card was sold by merchant.
     *
     * @param blueprintId
     * @param currentTime
     * @param price
     */
    void cardSold(String blueprintId, Date currentTime, int price);

    /**
     * Called when card was bought by merchant.
     *
     * @param blueprintId
     * @param currentTime
     * @param price
     */
    void cardBought(String blueprintId, Date currentTime, int price);
}
