package com.gempukku.stccg.common.filterable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public enum Affiliation implements Filterable {
    BAJORAN("Bajoran"),
    BORG("Borg"),
    CARDASSIAN("Cardassian"),
    DOMINION("Dominion"),
    FEDERATION("Federation"),
    FERENGI("Ferengi"),
    HIROGEN("Hirogen"),
    KAZON("Kazon"),
    KLINGON("Klingon"),
    NEUTRAL("Neutral"),
    NON_ALIGNED("Non-Aligned"),
    ROMULAN("Romulan"),
    STARFLEET("Starfleet"),
    VIDIIAN("Vidiian"),
    VULCAN("Vulcan"),
    XINDI("Xindi");

    private final String humanReadable;

    Affiliation(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return this.humanReadable;
    }

    public static Affiliation findAffiliation(String name) {
        Stream<Affiliation> affiliationValues = Arrays.stream(values());
        return affiliationValues.filter(
                        affiliation -> affiliation.getHumanReadable().equalsIgnoreCase(name) ||
                                affiliation.toString().equals(
                                        name.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_')))
                .findFirst().orElse(null);
    }

}