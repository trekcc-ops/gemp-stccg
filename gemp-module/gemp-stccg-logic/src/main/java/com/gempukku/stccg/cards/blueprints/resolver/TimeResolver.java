package com.gempukku.stccg.cards.blueprints.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;

public class TimeResolver {

    public static Time resolveTime(JsonNode value, String defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return parseTime(defaultValue.toLowerCase());
        if (value.isTextual())
            return parseTime(value.textValue().toLowerCase());
        throw new InvalidCardDefinitionException("Unable to resolve time: " + value);
    }


    private static Time parseTime(String value) throws InvalidCardDefinitionException {
        final String phaseName = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
        if (value.toLowerCase().startsWith("end(") && value.endsWith(")")) {
            if (phaseName.equalsIgnoreCase("current"))
                return new Time(null, false);
            return new Time(Phase.findPhase(phaseName), false);
        }
        else if (value.equalsIgnoreCase("endofturn"))
            return new Time(null, true);
        else {
            throw new InvalidCardDefinitionException("Unable to resolve time: " + value);
        }
    }

    public static class Time {
        private final Phase phase;
        private final boolean endOfTurn;

        private Time(Phase phase, boolean endOfTurn) {
            this.phase = phase;
            this.endOfTurn = endOfTurn;
        }

        public Phase getPhase() {
            return phase;
        }

        public boolean isEndOfTurn() {
            return endOfTurn;
        }

        public String getHumanReadable() {
            if (endOfTurn) {
                return "the end of the turn";
            }
            else if (phase == null) {
                return "the end of the current phase";
            }
            else {
                return "the end of the " + phase + " phase";
            }
        }
    }
}