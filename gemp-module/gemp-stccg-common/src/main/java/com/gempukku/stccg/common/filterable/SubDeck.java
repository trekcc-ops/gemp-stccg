package com.gempukku.stccg.common.filterable;

@SuppressWarnings("unused")
public enum SubDeck implements Filterable {
    MISSIONS("missions"),
    SEED_DECK("seed+dil"),
    DRAW_DECK("deck"),
    SITES("sites"),
    @SuppressWarnings("SpellCheckingInspection") QS_TENT("qstent"),
    QS_TENT_REFEREE("ref"),
    DILEMMA("dyson"),
    Q_FLASH("flash"),
    BATTLE_BRIDGE("tactics"),
    TRIBBLE("tribbles");

    private final String _lackeyName;
    SubDeck(String lackeyName) {
        _lackeyName = lackeyName;
    }
    public String getLackeyName() { return _lackeyName; }
}