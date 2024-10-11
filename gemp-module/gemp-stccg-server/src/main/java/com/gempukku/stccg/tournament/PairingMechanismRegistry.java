package com.gempukku.stccg.tournament;

public class PairingMechanismRegistry {
    public static PairingMechanism getPairingMechanism(String pairingType) {
        return switch(pairingType) {
            case "singleElimination" -> new SingleEliminationPairing(pairingType);
            case "swiss" -> new SwissPairingMechanism(pairingType);
            case "swiss-3" -> new SwissPairingMechanism(pairingType, 3);
            default -> null;
        };
    }
}