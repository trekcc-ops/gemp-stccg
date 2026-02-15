package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public interface CardWithStrength extends CardWithAttributes {

    Integer getIntegrity(DefaultGame cardGame);
    Integer getStrength(DefaultGame cardGame);
    Integer getCunning(DefaultGame cardGame);

    default int getTotalAttributes(DefaultGame cardGame) {
        return Objects.requireNonNullElse(getIntegrity(cardGame), 0) +
                Objects.requireNonNullElse(getStrength(cardGame), 0) +
                Objects.requireNonNullElse(getCunning(cardGame), 0);
    }

}