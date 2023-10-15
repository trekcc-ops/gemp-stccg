package com.gempukku.stccg.common.filterable;

public enum Affiliation implements Filterable {
    BAJORAN("Bajoran"), BORG("Borg"), CARDASSIAN("Cardassian"),
    FEDERATION("Federation"), FERENGI("Ferengi"), HIROGEN("Hirogen"),
    KAZON("Kazon"), KLINGON("Klingon"), NEUTRAL("Neutral"),
    NON_ALIGNED("Non-Aligned"), ROMULAN("Romulan"), STARFLEET("Starfleet"),
    VIDIIAN("Vidiian");

    private final String _humanReadable;
    Affiliation(String humanReadable) {
        _humanReadable = humanReadable;
    }
    public String getHumanReadable() { return _humanReadable; }
}
