package com.gempukku.stccg.common.filterable;

import java.util.Locale;

public enum Phase {
    // Generic
    BETWEEN_TURNS("Between turns"),
    // 1E
    SEED_DOORWAY("Doorway seed phase"),
    SEED_MISSION("Mission seed phase"),
    SEED_DILEMMA("Dilemma seed phase"),
    SEED_FACILITY("Facility seed phase"),
    CARD_PLAY("Card play"),
    EXECUTE_ORDERS("Execute orders"),
    END_OF_TURN("End of turn"),
    // Tribbles
    TRIBBLES_TURN("Tribbles turn"),

    // LotR
    FELLOWSHIP("Fellowship"),
    MANEUVER("Maneuver"),
    ARCHERY("Archery"),
    ASSIGNMENT("Assignment"),
    REGROUP("Regroup");

    private final String humanReadable;

    Phase(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public static Phase findPhase(String name) {
        String nameCaps = name.toUpperCase(Locale.ROOT).strip().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase(Locale.ROOT);

        for (Phase phase : values()) {
            String phaseStringLower = phase.toString().toLowerCase(Locale.ROOT);
            String phaseStringUpper = phase.toString();
            if (phaseStringLower.equals(nameLower) || phaseStringUpper.equals(nameCaps))
                return phase;
        }
        return null;
    }

    public boolean isSeedPhase() {
        return name().startsWith("SEED");
    }

    public String toString() { return humanReadable; }

}