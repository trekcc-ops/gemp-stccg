package com.gempukku.stccg.effectappender.resolver;

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
        if (value.toLowerCase().startsWith("start(") && value.endsWith(")")) {
            return new Time(Phase.findPhase(phaseName), true, false);
        }
        else if (value.toLowerCase().startsWith("end(") && value.endsWith(")")) {
            if (phaseName.equalsIgnoreCase("current"))
                return new Time(null, false, false);
            return new Time(Phase.findPhase(phaseName), false, false);
        }
        else if (value.equalsIgnoreCase("endofturn"))
            return new Time(null, false, true);
        else {
            Phase phase = Phase.findPhase(value);
            if(phase != null)
                return new Time(phase, true, false);
        }

        throw new InvalidCardDefinitionException("Unable to resolve time: " + value);
    }

    public static class Time {
        private final Phase phase;
        private final boolean start;
        private final boolean endOfTurn;

        private Time(Phase phase, boolean start, boolean endOfTurn) {
            this.phase = phase;
            this.start = start;
            this.endOfTurn = endOfTurn;
        }

        public Phase getPhase() {
            return phase;
        }

        public boolean isStart() {
            return start;
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
            else if (start) {
                return "the start of the " + phase.getHumanReadable() + " phase";
            }
            else {
                return "the end of the " + phase.getHumanReadable() + " phase";
            }
        }
    }
}
