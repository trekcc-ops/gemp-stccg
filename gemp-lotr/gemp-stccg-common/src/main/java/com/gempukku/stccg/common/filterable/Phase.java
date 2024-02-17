package com.gempukku.stccg.common.filterable;

public enum Phase {
    // Generic
    BETWEEN_TURNS("Between turns", false, false),
    // 1E
    SEED_DOORWAY("Doorway seed phase", true, true),
    SEED_MISSION("Mission seed phase", true, true),
    SEED_DILEMMA("Dilemma seed phase", true, true),
    SEED_FACILITY("Facility seed phase", true, true),
    PLAY("Play phase", true, true),
    CARD_PLAY("Card play", true, true),
    EXECUTE_ORDERS("Execute orders", true, true),
    // Tribbles
    TRIBBLES_TURN("Tribbles turn", true, true),

    // LotR
    FELLOWSHIP("Fellowship", true, true),
    SHADOW("Shadow", true, true),
    MANEUVER("Maneuver", true, true),
    ARCHERY("Archery", true, true),
    ASSIGNMENT("Assignment", true, true),
    SKIRMISH("Skirmish", true, true),
    REGROUP("Regroup", true, true);

    private final String humanReadable;
    private final boolean realPhase;
    private final boolean cardsAffectGame;

    Phase(String humanReadable, boolean realPhase, boolean cardsAffectGame) {
        this.humanReadable = humanReadable;
        this.realPhase = realPhase;
        this.cardsAffectGame = cardsAffectGame;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public boolean isRealPhase() {
        return realPhase;
    }

    public boolean isCardsAffectGame() {
        return cardsAffectGame;
    }

    public static Phase findPhase(String name) {
        String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();

        for (Phase phase : values()) {
            if (phase.getHumanReadable().toLowerCase().equals(nameLower) || phase.toString().equals(nameCaps))
                return phase;
        }
        return null;
    }
}
