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

    /* TODO: Implement "outside" and "aside" from Lackey? Might be nice for user experience,
        but these really just exist in Lackey for ease of use. Gemp will be able to create "from outside the game"
        cards on command if a legal card play, and quickly retrieve seed downloads.
     */

    private final String _lackeyName;
    SubDeck(String lackeyName) {
        _lackeyName = lackeyName;
    }
    public String getLackeyName() { return _lackeyName; }
}