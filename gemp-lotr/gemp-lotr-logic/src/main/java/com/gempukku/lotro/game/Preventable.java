package com.gempukku.lotro.game;

public interface Preventable {
    void prevent();

    boolean isPrevented(DefaultGame game);
}
